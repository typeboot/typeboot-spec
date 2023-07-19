package com.typeboot.dataformat.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.typeboot.dataformat.scripts.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

class ProviderConfig @JsonCreator constructor(val name: String, val options: Map<String, String>?)


data class OutputOptions @JsonCreator constructor(
    val path: String = "",
    val pad: String = "0",
    val prefix: String = "V"
)

data class SpecConfig @JsonCreator constructor(
    val provider: ProviderConfig,
    val mode: String,
    val generate: String,
    val source: String,
    val output: OutputOptions = OutputOptions()
) {
    fun getGenerators(): List<String> {
        return (if (generate == "all") {
            "mutations,audit"
        } else {
            generate
        }).split(",").toList()
    }

    fun getSources(): List<FileScript> {
        return FileScripts.fromSource(
            source,
            DefaultScriptNumberProvider(Pattern.compile("^[V]?([0-9\\.]+)(.*)\\.yaml$"))
        )
    }

    fun getTemplates(): List<FileScript> {
        val incremental = Incremental()
        val incNumberProvider = object : ScriptNumberProvider {
            override fun include(name: String): Boolean {
                return name.endsWith(".yaml")
            }

            override fun scriptForName(name: String): ScriptName {
                return ScriptName(incremental.next(), name);
            }
        }
        return FileScripts.fromSource("${source}/templates", incNumberProvider)
    }

}

class Incremental : Iterator<Int> {
    private val ref = AtomicInteger(0)
    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): Int {
        return ref.getAndIncrement()
    }
}
