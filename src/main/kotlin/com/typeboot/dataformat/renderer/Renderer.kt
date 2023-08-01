package com.typeboot.dataformat.renderer

import com.typeboot.dataformat.scripts.FileScript
import com.typeboot.dataformat.types.Instructions
import com.typeboot.dataformat.types.OutputOptions
import com.typeboot.dataformat.types.Serialisation
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class RenderOptions(val path: String, val paddedLength: Int, val prefix: String)

interface Renderer {
    fun render(fileScript: FileScript, instructions: List<Instructions>)
    fun render(instructions: List<Instructions>)
    fun preRender(fileScript: FileScript)
    fun postRender(fileScript: FileScript)
    fun renderInstructions(instructions: Instructions)

    companion object Factory {
        fun create(outputOptions: OutputOptions, serialisation: Serialisation): Renderer {
            return TextStreamRenderer(outputOptions, serialisation)
        }
    }
}


class TextStreamRenderer(private val options: OutputOptions, private val serialisation: Serialisation) : Renderer {
    private val renderOptions: RenderOptions = initRenderOptions()
    lateinit var fso: OutputStream

    private fun initRenderOptions(): RenderOptions {
        var effectivePath = ""
        options.path.let { path ->
            if (path.isNotEmpty()) {
                val out = File(path)
                if (!out.exists()) {
                    out.mkdirs()
                }
                effectivePath = path
            }
        }
        val paddedLength = (options.pad).toInt()
        val prefix = options.prefix
        return RenderOptions(effectivePath, paddedLength, prefix)
    }

    override fun render(fileScript: FileScript, instructions: List<Instructions>) {
        this.preRender(fileScript)
        this.render(instructions)
        this.postRender(fileScript)
    }

    override fun render(instructions: List<Instructions>) {
        instructions.forEach { ins ->
            renderInstructions(ins)
        }
    }

    override fun renderInstructions(instructions: Instructions) {
        fso.write("${instructions.text().trim()};\n".toByteArray())
        fso.flush()
    }

    override fun preRender(fileScript: FileScript) {
        fso = if (renderOptions.path.isEmpty()) {
            System.out
        } else {
            val subPathDir = File(this.renderOptions.path + "/" + serialisation.subPath)
            subPathDir.mkdirs()
            val paddedLength = renderOptions.paddedLength
            FileOutputStream(
                this.renderOptions.path + "/" + serialisation.subPath + "/" + fileScript.getPaddedFileName(
                    renderOptions.prefix,
                    paddedLength
                ) + serialisation.extension
            )
        }
    }

    override fun postRender(fileScript: FileScript) {
        fso.flush()
        fso.close()
    }
}

