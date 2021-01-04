package com.typeboot.dataformat.factory

import com.typeboot.dataformat.config.YamlSupport
import com.typeboot.dataformat.renderer.RendererFactory
import com.typeboot.dataformat.types.*

class TypeFactory(name: String = ".typeboot.yaml") {
    private val yaml = YamlSupport()
    private val spec = yaml.toInstance(name, SpecConfig::class.java)

    fun generate() {
        val renderers = spec.getRenderers()
        renderers.forEach { name ->
            val renderer = RendererFactory.render(name)
            spec.getSources().forEach { fileScript ->
                val fileName = fileScript.filePath
                val tables = yaml.toList(fileName)
                tables.forEach { table ->
                    val tableDef = table.toPrettyString()
                    val data = tableDef.toByteArray()
                    when (table.get("kind").textValue()) {
                        "SchemaDefinition" -> renderer.renderSchema(yaml.toInstance(data, SchemaDefinition::class.java))
                        "TableDefinition" -> renderer.renderTable(yaml.toInstance(data, TableDefinition::class.java))
                        "ColumnDefinition" -> renderer.renderColumn(yaml.toInstance(data, ColumnDefinition::class.java))
                        "ColumnRemovalDefinition" -> renderer.renderColumnRemoval(yaml.toInstance(data, ColumnRemovalDefinition::class.java))
                        "TableRemovalDefinition" -> renderer.renderTableRemoval(yaml.toInstance(data, TableRemovalDefinition::class.java))
                        "ColumnRenameDefinition" -> renderer.renderColumnRename(yaml.toInstance(data, ColumnRenameDefinition::class.java))
                    }
                }
            }
        }
    }
}