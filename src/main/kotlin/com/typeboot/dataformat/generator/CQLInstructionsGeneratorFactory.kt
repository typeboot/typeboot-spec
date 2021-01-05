package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

class CQLInstructionsGeneratorFactory(private val options: Map<String, String>) : Generator {

    private fun schemaName(schema: String): String = if (options["dynamic_schema"] ?: "no" == "yes") "{{keyspace:$schema}}" else schema

    override fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions> {
        val replication = schemaDefinition.options.replicas.joinToString(separator = ", ",
                transform = { r -> "'${r.datacenterName}': ${r.replica}" }
        )
        val schemaData = schemaName(schemaDefinition.subject.schema)
        return listOf(Instructions("create keyspace if not exists $schemaData with replication={$replication}"))
    }

    override fun generateTable(tableDefinition: TableDefinition): List<Instructions> {
        println("generate cassandra table ddl")
        val schemaData = schemaName(tableDefinition.subject.schema)
        val table = tableDefinition.subject.table
        val columns = tableDefinition.fields.flatMap { f ->
            val columnName = f.name
            val type = f.type ?: "text"
            listOf("  $columnName $type")
        }.joinToString(separator = ",\n")

        val primaryKey = tableDefinition.constraints.filter { c -> c.type.toLowerCase() == "primary key" }.map { c ->
            c.fieldNames.joinToString(prefix = "  PRIMARY KEY(", separator = ", ", postfix = ")")
        }.joinToString("")

        val columnSpec = listOf(columns, primaryKey).filter{ s -> s.isNotEmpty()}.joinToString(separator = ",\n")

        val options = tableDefinition.options.orEmpty().map { kv ->
            val value = kv.value.toString().replace("\n", " ").trim()
            "${kv.key}=$value"
        }.joinToString(separator = "\n AND ")

        val clustering = tableDefinition.constraints.filter { c -> c.type.toLowerCase() == "clustering key" }.map { c ->
            c.fieldNames.map { keyName ->
                if (keyName.startsWith("-")) "${keyName.substring(1)} DESC" else "$keyName ASC"
            }.joinToString(prefix = "CLUSTERING ORDER BY(", separator = ", ", transform = { s -> s }, postfix = ")\n")
        }.joinToString(separator = "")

        val tableOptionsList = listOf(clustering, options).filter{s -> s.trim().isNotEmpty()}
        val withPrefix = if(tableOptionsList.isNotEmpty()) " WITH " else ""
        val tableOptions = tableOptionsList.joinToString(prefix = withPrefix, separator = " AND ")
        val instruction = "\ncreate table $schemaData.$table (\n$columnSpec\n)$tableOptions"

        return listOf(Instructions(instruction))
    }

    override fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions> {
        println("generate cassandra column ddl")
        return listOf()
    }

    override fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions> {
        println("generate cassandra column drop")
        return listOf()
    }

    override fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions> {
        println("generate cassandra column rename")
        return listOf()
    }

    override fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions> {
        println("generate cassandra table remove")
        return listOf()
    }

    override fun serialisationProps(): Serialisation {
        return Serialisation("cql", ".cql")
    }
}