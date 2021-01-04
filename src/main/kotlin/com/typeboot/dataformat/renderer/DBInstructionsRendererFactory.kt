package com.typeboot.dataformat.renderer

import com.typeboot.dataformat.types.*

class DBInstructionsRendererFactory : Renderer {
    override fun renderSchema(schemaDefinition: SchemaDefinition) {
        println("generate schema ddl")
    }

    override fun renderTable(tableDefinition: TableDefinition) {
        println("generate table ddl")
    }

    override fun renderColumn(columnDefinition: ColumnDefinition) {
     println("generate column ddl")
    }

    override fun renderColumnRemoval(columnRemovalDefinition: ColumnRemovalDefinition) {
        println("generate column drop")
    }

    override fun renderColumnRename(columnRenameDefinition: ColumnRenameDefinition) {
        println("generate column rename")
    }

    override fun renderTableRemoval(tableRemovalDefinition: TableRemovalDefinition) {
        println("generate table remove")
    }
}