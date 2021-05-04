package com.typeboot.dataformat.scripts

import java.io.File
import java.util.regex.Pattern

data class FileScript(val serial: Int, val name: String, val filePath: String) {
    fun getParent():String {
        return File(filePath).parent
    }
}

interface ScriptNumberProvider {
    fun include(name: String): Boolean
    fun scriptForName(name: String): Int
}

class DefaultScriptNumberProvider(private val reg: Pattern) : ScriptNumberProvider {
    override fun include(name: String): Boolean {
        return reg.matcher(name).matches()
    }

    override fun scriptForName(name: String): Int {
        val matcher = reg.matcher(name)
        matcher.matches()
        return matcher.toMatchResult().group(1).toInt()
    }
}

class FileScripts {
    companion object {


        fun fromSource(source: String, scriptNumberProvider: ScriptNumberProvider): List<FileScript> {
            val dataFiles = File(source).walk().filter { f ->
                f.isFile && scriptNumberProvider.include(f.name)
            }.toList()
            val fileCache = mutableMapOf<String, File>()
            val scriptCache = mutableListOf<Int>()
            return dataFiles.map { f ->
                if (fileCache.containsKey(f.name)) {
                    throw RuntimeException("duplicate script name ${f.name}")
                } else {
                    fileCache[f.name] = f
                }
                val scriptNumber = scriptNumberProvider.scriptForName(f.name)
                if (scriptCache.contains(scriptNumber)) {
                    throw RuntimeException("duplicate script number $scriptNumber in ${f.name} as it already exists.")
                } else {
                    scriptCache.add(scriptNumber)
                }
                FileScript(scriptNumber, f.name, f.absolutePath)
            }.sortedBy { fs -> fs.serial }
        }
    }
}