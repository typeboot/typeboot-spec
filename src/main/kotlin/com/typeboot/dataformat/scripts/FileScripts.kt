package com.typeboot.dataformat.scripts

import java.io.File
import java.util.regex.Pattern

data class FileScript(val serial: Int, val name: String, val filePath: String)

class FileScripts {
    companion object {
        private fun pattern(ext: String): Pattern = Pattern.compile("([0-9]+).*\\.$ext")

        fun fromSource(source: String, ext: String): List<FileScript> {
            val reg = pattern(ext)
            val dataFiles = File(source).walk().filter { f ->
                f.isFile && reg.matcher(f.name).matches()
            }.toList()
            val fileCache = mutableMapOf<String, File>()
            val scriptCache = mutableListOf<Int>()
            return dataFiles.map { f ->
                if (fileCache.containsKey(f.name)) {
                    throw RuntimeException("duplicate script name ${f.name}")
                } else {
                    fileCache[f.name] = f
                }
                val matcher = reg.matcher(f.name)
                matcher.matches()
                val scriptNumber = matcher.toMatchResult().group(1).toInt()
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