package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

class DBInstructionsGeneratorFactory(private val options:Map<String, String>) : Generator {

    override fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions> {
        println("generate schema ddl")
        return listOf(Instructions("test"))
    }

    override fun generateTable(tableDefinition: TableDefinition): List<Instructions> {
        println("generate table ddl")
        return listOf()
    }

    override fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions> {
        println("generate column ddl")
        return listOf()
    }

    override fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions> {
        println("generate column drop")
        return listOf()
    }

    override fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions> {
        println("generate column rename")
        return listOf()
    }

    override fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions> {
        println("generate table remove")
        return listOf()
    }

    override fun serialisationProps(): Serialisation {
        return Serialisation("sql", ".sql")
    }
}