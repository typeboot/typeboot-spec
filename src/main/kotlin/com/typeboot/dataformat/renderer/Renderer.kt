package com.typeboot.dataformat.renderer

import com.typeboot.dataformat.scripts.FileScript
import com.typeboot.dataformat.types.Instructions
import com.typeboot.dataformat.types.Serialisation
import java.io.File
import java.io.FileOutputStream

class RenderOptions(val path: String, val paddedLength: Int)

interface Renderer {
    fun render(fileScript: FileScript, instructions: List<Instructions>, serialisation: Serialisation)

    companion object Factory {
        fun create(outputOptions: Map<String, String>?): Renderer {
            return TextRenderer(outputOptions ?: mapOf("path" to "", "pad" to "0"))
        }
    }
}

class TextRenderer(private val options: Map<String, String>) : Renderer {
    private val renderOptions: RenderOptions = initRenderOptions()
    private fun initRenderOptions(): RenderOptions {
        var effectivePath = ""
        options["path"]?.let { path ->
            if (path.isNotEmpty()) {
                val out = File(path)
                if (!out.exists()) {
                    out.mkdirs()
                }
                effectivePath = path
            }
        }
        val paddedLength = (options["pad"] ?:"4").toInt()
        return RenderOptions(effectivePath, paddedLength)
    }

    override fun render(fileScript: FileScript, instructions: List<Instructions>, serialisation: Serialisation) {
        val fso = if (renderOptions.path.isEmpty()) {
            System.out
        } else {
            val subPathDir = File(this.renderOptions.path + "/" + serialisation.subPath)
            subPathDir.mkdirs()
            val paddedLength = renderOptions.paddedLength
            FileOutputStream(this.renderOptions.path + "/" + serialisation.subPath + "/" + fileScript.getPaddedFileName(paddedLength) + serialisation.extension)
        }
        instructions.forEach { ins ->
            fso.write("${ins.text()};\n".toByteArray())
            fso.flush()
        }
    }
}
