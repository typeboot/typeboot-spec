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
            val default = if (it.default != null) " default ${it.default}"
            else ""
            val constraint = if (it.constraint != null)
                it.constraint.joinToString(prefix = " ", separator = " ")
            else ""

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

        val fieldConstraintSep = if (tableDefinition.constraints.isNotEmpty()) ",\n\n"
        else ""
        val tableFields = constructTableFields(tableDefinition)
                .joinToString(separator = ",\n", postfix = fieldConstraintSep)
        val constraintsAndFields = constructConstraintFields(tableDefinition)
            .joinToString(prefix = "$tableFields", separator = ",\n", postfix = "\n")
        val myInstruction = "\ncreate table ${tableDefinition.subject.schema}." +
                "${tableDefinition.subject.table} (\n${constraintsAndFields})"

        return listOf(Instructions(myInstruction))
    }

//    issue two semicolons generated
    override fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions> {
        println("generate column ddl")
        val table = "${columnDefinition.subject.schema}.${columnDefinition.subject.table}"
        val columnAndType = columnDefinition.fields.joinToString(separator = ", ") {
            val type = it.type ?: "text"
            "ADD COLUMN ${it.name} $type"
        }
        val myInstruction = "\nALTER TABLE $table $columnAndType"
        return listOf(Instructions(myInstruction))
    }

//    need to add something to the yaml file like options to allow people to make use of 'cascade'
//    also allow for an 'if exists'
    override fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions> {
        println("generate column drop")
        val table = "${columnRemovalDefinition.subject.schema}" +
                ".${columnRemovalDefinition.subject.table}"
        val column = columnRemovalDefinition.fields.joinToString(separator = ", ") {
            "DROP COLUMN ${it.name}"
        }
        val myInstruction = "\nALTER TABLE $table $column"
        return listOf(Instructions(myInstruction))
    }

    override fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions> {
        println("generate column rename")
//        val table = "${columnRenameDefinition.subject.schema}" +
//                ".${columnRenameDefinition.subject.table}"
//        val column = columnRenameDefinition.fields.joinToString(separator = ", ") {
//            "RENAME ${it.oldName} to ${it.newName}"
//        }
//
//        val myInstruction = "\nALTER TABLE $table $column"
//        return listOf(Instructions(myInstruction))
        return listOf()
    }

//    DROP TABLE [IF EXISTS] table_name
//[CASCADE | RESTRICT];
    override fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions> {
        println("generate table remove")
        val table = "${tableRemovalDefinition.subject.schema}" +
                ".${tableRemovalDefinition.subject.table}"
        val myInstruction = "\nDROP TABLE $table"
        return listOf(Instructions(myInstruction))
    }

    override fun serialisationProps(): Serialisation {
        return Serialisation("sql", ".sql")
    }
}