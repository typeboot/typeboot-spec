package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

class DBInstructionsGeneratorFactory(private val options: Map<String, String>) : Generator {

    override fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions> {
        println("generate schema ddl")
        val schema = schemaDefinition.subject.schema
        return listOf(Instructions("create schema $schema"))
    }

    private fun constructTableFields(tableDefinition: TableDefinition):
            List<String> {
        return tableDefinition.fields.map {
            val type = it.type ?: "text"
            val default = it.default?.let{d->" default $d"} ?: ""
            val constraint = it.constraint?.joinToString(prefix = " ", separator = " ") ?: ""
            "${it.name} $type$constraint$default"


        }
    }

    private fun constructConstraintFields(tableDefinition: TableDefinition):
            List<String> {
        return tableDefinition.constraints.filter {
            when (it.type) {
                "primary key", "foreign key", "unique" -> true
                else -> false
            }
        }.map {
            val fields = when (it.type) {
                "primary key", "unique" -> it.fieldNames.joinToString(prefix = "(", separator = ", ", postfix = ")")
                "foreign key" ->
                    "(${it.fieldNames.joinToString(separator = ", ")}) " +
                            "references ${tableDefinition.subject.schema}.${it.reference?.table} (${it
                                    .reference?.fieldNames?.joinToString(separator = ", ")})"
                else -> ""
            }
            "${it.type.toUpperCase()} $fields"
        }
    }

    override fun generateTable(tableDefinition: TableDefinition): List<Instructions> {
        println("generate table ddl")
        val schema = "${tableDefinition.subject.schema}"
        val table = "${tableDefinition.subject.table}"
        val fieldConstraintSep = if (tableDefinition.constraints.isNotEmpty()) ",\n"
        else ""
        val tableFields = constructTableFields(tableDefinition)
                .joinToString(separator = ",\n", postfix = fieldConstraintSep)
        val constraintsAndFields = constructConstraintFields(tableDefinition)
            .joinToString(prefix = "$tableFields", separator = ",\n", postfix = "\n")
        val myInstruction = "\ncreate table $schema.$table (\n${constraintsAndFields})"
        return listOf(Instructions(myInstruction))
    }

//    issue two semicolons generated
    override fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions> {
        println("generate column ddl")
        val schema = "${columnDefinition.subject.schema}"
        val table = "${columnDefinition.subject.table}"
        val columnAndType = columnDefinition.fields.joinToString(separator = ", ") {
            val type = it.type ?: "text"
            "ADD COLUMN ${it.name} $type"
        }
        val myInstruction = "\nALTER TABLE $schema.$table $columnAndType"
        return listOf(Instructions(myInstruction))
    }

//    need to add something to the yaml file like options to allow people to make use of 'cascade'
//    also allow for an 'if exists'
    override fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions> {
        println("generate column drop")
        val schema = "${columnRemovalDefinition.subject.schema}"
        val table = "${columnRemovalDefinition.subject.table}"
        val column = columnRemovalDefinition.fields.joinToString(separator = ", ") {
            "DROP COLUMN ${it.name}"
        }
        val myInstruction = "\nALTER TABLE $schema.$table $column"
        return listOf(Instructions(myInstruction))
    }

    override fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions> {
        println("generate column rename")
        val schema = "${columnRenameDefinition.subject.schema}"
        val table = "${columnRenameDefinition.subject.table}"
        return columnRenameDefinition.fields.map {
            Instructions("\nALTER TABLE $schema.$table RENAME ${it.from} to ${it.to}")
        }
    }

    //DROP TABLE [IF EXISTS] table_name
    //[CASCADE | RESTRICT];
    override fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions> {
        println("generate table remove")
        val schema = "${tableRemovalDefinition.subject.schema}"
        val table = "${tableRemovalDefinition.subject.table}"
        val myInstruction = "\nDROP TABLE $schema.$table"
        return listOf(Instructions(myInstruction))
    }

    override fun serialisationProps(): Serialisation {
        val ext = options["ext"]?:".sql"
        return Serialisation("sql", ext)
    }

}