package com.typeboot.dataformat.renderer

import com.typeboot.dataformat.types.*

class DBMetadataRendererFactory : Renderer {
    override fun renderSchema(schemaDefinition: SchemaDefinition) {
        println("generate schema metadata")
    }

    override fun renderTable(tableDefinition: TableDefinition) {
        println("generate table metadata")
    }

    override fun renderColumn(columnDefinition: ColumnDefinition) {
        println("generate column metadata")
    }

    override fun renderColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition) {
        println("generate column remove metadata")
    }

    override fun renderColumnRename(columnRenameDefinition: ColumnRenameDefinition) {
        println("generate column rename metadata")
    }

    override fun renderTableRemoval(tableRemovalDefinition: TableRemovalDefinition) {
        println("generate table remove metadata")
    }
}