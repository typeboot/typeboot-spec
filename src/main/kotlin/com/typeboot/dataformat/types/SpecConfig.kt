package com.typeboot.dataformat.types

import com.typeboot.dataformat.scripts.DefaultScriptNumberProvider
import com.typeboot.dataformat.scripts.FileScript
import com.typeboot.dataformat.scripts.FileScripts
import com.typeboot.dataformat.scripts.ScriptNumberProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

class ProviderConfig(val name: String, val options: Map<String, String>?)
data class SpecConfig(val provider: ProviderConfig,
                      val mode: String,
                      val generate: String,
                      val source: String,
                      val output: String) {
    fun getGenerators(): List<String> {
        return (if (generate == "all") {
            "mutations,audit"
        } else {
            generate
        }).split(",").toList()
    }

    fun getSources(): List<FileScript> {
        return FileScripts.fromSource(source, DefaultScriptNumberProvider(Pattern.compile("([0-9]+).*\\.yaml")))
    }

    fun getTemplates(): List<FileScript> {
        val incremental = Incremental()
        val incNumberProvider = object : ScriptNumberProvider {
            override fun include(name: String): Boolean {
                return name.endsWith(".yaml")
            }

            override fun scriptForName(name: String): Int {
                return incremental.next()
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
