package com.typeboot.dataformat.scripts

import java.io.File
import java.util.regex.Pattern

data class FileScript(private val scriptName: ScriptName, val filePath: String) {
    fun getParent(): String {
        return File(filePath).parent
    }

    fun getFileName(): String {
        return "${scriptName.serial}${scriptName.name}"
    }

    fun getPaddedFileName(prefix: String, desiredSerialLength: Int): String {
        val paddedSerial = if (desiredSerialLength > 0) "${scriptName.serial}".padStart(desiredSerialLength, '0') else scriptName.serial
        return "${prefix}${paddedSerial}${scriptName.name}"
    }

    fun getSerial(): Int {
        return scriptName.serial
    }
}

data class ScriptName(val serial: Int, val name: String)

interface ScriptNumberProvider {
    fun include(name: String): Boolean
    fun scriptForName(name: String): ScriptName
}

class DefaultScriptNumberProvider(private val reg: Pattern) : ScriptNumberProvider {
    override fun include(name: String): Boolean {
        return reg.matcher(name).matches()
    }

    override fun scriptForName(name: String): ScriptName {
        val matcher = reg.matcher(name)
        matcher.matches()
        val result = matcher.toMatchResult()
        return ScriptName(result.group(1).toInt(), result.group(2))
    }
}

class FileScripts {
    companion object {

        fun fromSource(source: String, scriptNumberProvider: ScriptNumberProvider): List<FileScript> {
            val dataFiles = File(source).walk().filter { f ->
                f.parentFile.name != "data" &&  f.isFile && scriptNumberProvider.include(f.name)
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
                if (scriptCache.contains(scriptNumber.serial)) {
                    throw RuntimeException("duplicate script number $scriptNumber in ${f.name} as it already exists.")
                } else {
                    scriptCache.add(scriptNumber.serial)
                }
                FileScript(scriptNumber, f.absolutePath)
            }.sortedBy { fs -> fs.getSerial() }
        }
    }
}