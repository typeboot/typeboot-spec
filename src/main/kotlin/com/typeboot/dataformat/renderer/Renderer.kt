package com.typeboot.dataformat.renderer

import com.typeboot.dataformat.types.FileScript
import com.typeboot.dataformat.types.Instructions
import com.typeboot.dataformat.types.Serialisation
import java.io.File
import java.io.FileOutputStream

interface Renderer {
    fun render(fileScript: FileScript, instructions: List<Instructions>, serialisation: Serialisation)

    companion object Factory {
        fun create(output: String): Renderer {
            return TextRenderer(output)
        }
    }
}

class TextRenderer(private val output: String) : Renderer {

    init {
        if (output.isNotEmpty()) {
            val out = File(output)
            if (!out.exists()) {
                out.mkdirs()
            }
        }
    }

    override fun render(fileScript: FileScript, instructions: List<Instructions>, serialisation: Serialisation) {
        val fso = if (this.output.isEmpty()) {
            System.out
        } else {
            val subPathDir = File(this.output + "/" + serialisation.subPath)
            subPathDir.mkdirs()
            FileOutputStream(this.output + "/" + serialisation.subPath + "/" + fileScript.name + serialisation.extension)
        }
        instructions.forEach { ins ->
            fso.write("${ins.text()};\n".toByteArray())
            fso.flush()
        }
    }
}
