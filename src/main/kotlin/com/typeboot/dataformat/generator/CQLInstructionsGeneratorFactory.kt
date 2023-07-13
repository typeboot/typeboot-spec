package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

class CQLInstructionsGeneratorFactory(private val options: Map<String, String>) : Generator {

    private fun schemaName(schema: String): String = if (options["dynamic_schema"] ?: "no" == "yes") "{{keyspace:$schema}}" else schema

    override fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions> {
        val replication = schemaDefinition.options.replicas?.let { rs ->
            rs.joinToString(separator = ", ", transform = { r -> "{'${r.datacenterName}': ${r.replica}, 'class': 'NetworkTopologyStrategy'}" })
        } ?: "${schemaDefinition.options.replication}"

        val schemaData = schemaName(schemaDefinition.subject.schema)
        return listOf(Instructions("create keyspace if not exists $schemaData with replication=$replication"))
    }

    override fun generateTable(tableDefinition: TableDefinition): List<Instructions> {
        println("generate cassandra table ddl")
        val schemaData = schemaName(tableDefinition.subject.schema)
        val table = tableDefinition.subject.table
        val columns = serialiseColumns(tableDefinition.fields)

        val primaryKey = tableDefinition.constraints.filter { c -> c.type.lowercase() == "primary key" }.map { c ->
            c.fieldNames.joinToString(prefix = "  PRIMARY KEY(", separator = ", ", postfix = ")")
        }.joinToString("")

        val columnSpec = listOf(columns, primaryKey).filter { s -> s.isNotEmpty() }.joinToString(separator = ",\n")

        val options = tableDefinition.options.orEmpty().map { kv ->
            val value = kv.value.toString().replace("\n", " ").trim()
            "${kv.key}=$value"
        }.joinToString(separator = "\n AND ")

        val clustering = tableDefinition.constraints.filter { c -> c.type.lowercase() == "clustering key" }.map { c ->
            c.fieldNames.map { keyName ->
                if (keyName.startsWith("-")) "${keyName.substring(1)} DESC" else "$keyName ASC"
            }.joinToString(prefix = "CLUSTERING ORDER BY(", separator = ", ", transform = { s -> s }, postfix = ")\n")
        }.joinToString(separator = "")

        val tableOptionsList = listOf(clustering, options).filter { s -> s.trim().isNotEmpty() }
        val withPrefix = if (tableOptionsList.isNotEmpty()) " WITH " else ""
        val tableOptions = tableOptionsList.joinToString(prefix = withPrefix, separator = " AND ")
        val instruction = "\ncreate table $schemaData.$table (\n$columnSpec\n)$tableOptions"

        return listOf(Instructions(instruction))
    }

    private fun serialiseColumns(fields: List<FieldDefinition>) = fields.flatMap { f ->
        val type = f.type ?: "text"
        listOf("  ${f.name} $type")
    }.joinToString(separator = ",\n")

    override fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions> {
        println("generate cassandra column ddl")
        val schema = schemaName(columnDefinition.subject.schema)
        val table = columnDefinition.subject.table
        val columnList = serialiseColumns(columnDefinition.fields)
        val alterStmt = "\nALTER TABLE $schema.$table ADD (\n$columnList\n)"
        return listOf(Instructions(alterStmt))
    }

    override fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions> {
        println("generate cassandra column drop")
        val schema = schemaName(columnRemovalDefinition.subject.schema)
        val table = columnRemovalDefinition.subject.table
        val columns = columnRemovalDefinition.fields.map { f -> f.name }.joinToString(separator = ", ")
        val dropCols = "\nALTER TABLE $schema.$table DROP ($columns)"
        return listOf(Instructions(dropCols))
    }

    override fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions> {
        println("generate cassandra column rename")
        val schema = schemaName(columnRenameDefinition.subject.schema)
        val table = columnRenameDefinition.subject.table
        return columnRenameDefinition.fields.map { r ->
            Instructions("\nALTER TABLE $schema.$table RENAME ${r.from} TO ${r.to}")
        }
    }

    override fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions> {
        println("generate cassandra table remove")
        val schema = schemaName(tableRemovalDefinition.subject.schema)
        val table = tableRemovalDefinition.subject.table
        val dropStmt = "\nDROP TABLE $schema.$table"
        return listOf(Instructions(dropStmt))
    }

    override fun serialisationProps(): Serialisation {
        val ext = options["ext"] ?: ".cql"
        return Serialisation("cql", ext)
    }

}
