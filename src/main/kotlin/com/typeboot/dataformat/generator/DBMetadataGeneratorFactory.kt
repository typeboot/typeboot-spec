package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

class DBMetadataGeneratorFactory : Generator {
    override fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions> {
        println("generate schema metadata")
        return listOf(Instructions("test metadata"))
    }

    override fun generateTable(tableDefinition: TableDefinition): List<Instructions> {
        println("generate table metadata")
        return listOf()
    }

    override fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions> {
        println("generate column metadata")
        return listOf()
    }

    override fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions> {
        println("generate column remove metadata")
        return listOf()
    }

    override fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions> {
        println("generate column rename metadata")
        return listOf()
    }

    override fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions> {
        println("generate table remove metadata")
        return listOf()
    }

    override fun serialisationProps(): Serialisation {
        return Serialisation("metadata", ".metadata.json")
    }

}