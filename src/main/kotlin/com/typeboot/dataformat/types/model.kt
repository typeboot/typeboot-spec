package com.typeboot.dataformat.types

import com.fasterxml.jackson.annotation.JsonProperty

class Subject(val schema: String, val table: String?)
class ReplicationConfig(@JsonProperty("datacenter-name") val datacenterName: String, val replica: Int)

class SchemaOptions(val replicas: List<ReplicationConfig>)

data class SchemaDefinition(val kind: String, val subject: Subject, val options: SchemaOptions)

class FieldDefinition(val name: String, val type: String?, val constraint:
List<String>?, val default: String?, val description: String?, val tags:
        List<String>?)

class ReferenceDefinition(val table:String, val fieldNames:List<String>)

class ConstraintDefinition(val type: String, val fieldNames: List<String>, val reference:ReferenceDefinition?)

data class TableDefinition(val kind: String, val subject: Subject,
                           val options: Map<String, *>?,
                           val fields: List<FieldDefinition>,
                           val constraints: List<ConstraintDefinition>
)


class RenameOption(val from: String, val to: String)
data class ColumnDefinition(val kind: String, val subject: Subject, val fields: List<FieldDefinition>)




data class ColumnRemovalDefinition(val kind: String, val subject: Subject, val fields: List<FieldDefinition>)
data class TableRemovalDefinition(val kind: String, val subject: Subject)
data class ColumnRenameDefinition(val kind: String, val subject: Subject, val fields: List<RenameOption>)

