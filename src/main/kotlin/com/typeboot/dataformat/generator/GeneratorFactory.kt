package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.ProviderConfig

class GeneratorFactory {
    companion object Factory {
        fun render(intent: String, provider: ProviderConfig): Generator {
            val options = let { provider.options }.orEmpty()
            println("intent $intent, provider: $provider, options: $options")

            return when (intent) {
                "audit" -> DBMetadataGeneratorFactory()
                else -> when (provider.name) {
                    "cassandra" -> CQLInstructionsGeneratorFactory(options)
                    "postgresql" -> DBInstructionsGeneratorFactory(options)
                    else -> DBInstructionsGeneratorFactory(options)
                }
            }
        }
    }
}
