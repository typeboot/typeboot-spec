package com.typeboot.dataformat.types

import com.typeboot.dataformat.scripts.FileScript
import com.typeboot.dataformat.scripts.FileScripts
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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
        return FileScripts.fromSource(source, "yaml")
    }

}