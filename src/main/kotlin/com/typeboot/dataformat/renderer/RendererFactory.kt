package com.typeboot.dataformat.renderer

class RendererFactory {
    companion object Factory {
        fun render(intent: String): Renderer {
            return when (intent) {
                "audit" -> DBMetadataRendererFactory()
                else -> DBInstructionsRendererFactory()
            }
        }
    }
}
