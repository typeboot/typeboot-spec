package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

class DBInstructionsGeneratorFactory(private val options:Map<String, String>) : Generator {

    override fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions> {
        println("generate schema ddl")
        val schema = schemaDefinition.subject.schema
        return listOf(Instructions("create schema $schema"))
    }

    override fun generateTable(tableDefinition: TableDefinition): List<Instructions> {
        println("generate table ddl")

        val fieldConstraintSep = if (tableDefinition.constraints.isEmpty()) {
            ""
        } else {
            ",\n\n"
        }

        val tableFields = tableDefinition.fields.joinToString(separator = "," +
                "\n", postfix = fieldConstraintSep) {
            val type = it.type ?: "text"

            var default = if (it.default != null) {
                " default ${it.default}"
            } else {
                ""
            }

            val constraint = if (it.constraint != null) {
                it.constraint.joinToString(prefix =" ", separator = " ")
            } else {
                ""
            }

            "${it.name} $type$constraint$default"
        }

        val constraints = tableDefinition.constraints.joinToString(prefix = "$tableFields", separator = ",\n", postfix =
        "\n") {
            val fields = when (it.type) {
                "primary key", "unique" -> it.fieldNames.joinToString(prefix = "(", separator = ", ", postfix = ")")
                "foreign key" ->
                    "(${it.fieldNames.joinToString(separator = ", ")}) " +
                            "references ${it.reference?.table} (${it
                                    .reference?.fieldNames?.joinToString(separator = ", ")})"
                else -> ""
            }
            "${it.type.toUpperCase()} $fields"
        }

        val myInstruction = "\ncreate table ${tableDefinition.subject.table} " +
                "(\n${constraints})"

        return listOf(Instructions(myInstruction))
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