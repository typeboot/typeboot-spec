package com.typeboot.dataformat.renderer

import com.typeboot.dataformat.types.*

interface Renderer {
    fun renderSchema(schemaDefinition: SchemaDefinition)
    fun renderTable(tableDefinition: TableDefinition)
    fun renderColumn(columnDefinition: ColumnDefinition)
    fun renderColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition)
    fun renderColumnRename(columnRenameDefinition: ColumnRenameDefinition)
    fun renderTableRemoval(tableRemovalDefinition: TableRemovalDefinition)
}