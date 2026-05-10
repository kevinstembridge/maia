package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class BlotterCompositePkColumnDef : AbstractBlotterColumnDef(
    columnHeader = "ID",
    cellRenderer = null,
    colId = "id",
    hide = true
) {


    val classFieldDef = aClassField(this.colId, fieldType = FieldTypes.string).build()


}
