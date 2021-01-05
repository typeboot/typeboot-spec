package com.typeboot.dataformat.generator

import com.typeboot.dataformat.types.*

interface Generator {
    fun generateSchema(schemaDefinition: SchemaDefinition): List<Instructions>
    fun generateTable(tableDefinition: TableDefinition): List<Instructions>
    fun generateColumn(columnDefinition: ColumnDefinition): List<Instructions>
    fun generateColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition): List<Instructions>
    fun generateColumnRename(columnRenameDefinition: ColumnRenameDefinition): List<Instructions>
    fun generateTableRemoval(tableRemovalDefinition: TableRemovalDefinition): List<Instructions>
    fun serialisationProps(): Serialisation
}