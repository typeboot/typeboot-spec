package com.typeboot.dataformat.factory

import com.fasterxml.jackson.databind.node.ObjectNode
import com.typeboot.dataformat.config.YamlSupport
import com.typeboot.dataformat.generator.GeneratorFactory
import com.typeboot.dataformat.renderer.Renderer
import com.typeboot.dataformat.types.*

class TypeFactory(name: String = ".typeboot.yaml") {
    private val yaml = YamlSupport()
    private val spec = yaml.toInstance(name, SpecConfig::class.java)

    fun generate() {
        val generators = spec.getGenerators()
        val renderer = Renderer.create(spec.output)
        generators.forEach { name ->
            val generator = GeneratorFactory.render(name, spec.provider)
            val serialisation = generator.serialisationProps()
            spec.getSources().forEach { fileScript ->
                val fileName = fileScript.filePath
                val tables = yaml.toList(fileName)
                val ins = tables.flatMap<ObjectNode, Instructions> { table ->
                    val tableDef = table.toPrettyString()
                    val data = tableDef.toByteArray()
                    when (table.get("kind").textValue()) {
                        "SchemaDefinition" -> generator.generateSchema(yaml.toInstance(data, SchemaDefinition::class.java))
                        "TableDefinition" -> generator.generateTable(yaml.toInstance(data, TableDefinition::class.java))
                        "ColumnDefinition" -> generator.generateColumn(yaml.toInstance(data, ColumnDefinition::class.java))
                        "ColumnRemovalDefinition" -> generator.generateColumnRemoval(yaml.toInstance(data, ColumnRemovalDefinition::class.java))
                        "TableRemovalDefinition" -> generator.generateTableRemoval(yaml.toInstance(data, TableRemovalDefinition::class.java))
                        "ColumnRenameDefinition" -> generator.generateColumnRename(yaml.toInstance(data, ColumnRenameDefinition::class.java))
                        else -> listOf()
                    }
                }
                renderer.render(fileScript, ins, serialisation)
            }
        }
    }
}