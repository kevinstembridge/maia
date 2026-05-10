package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


class BlotterIdColumnDef(
    val classFieldDef: ClassFieldDef
): AbstractBlotterColumnDef(
    columnHeader = null,
    cellRenderer = null,
    colId = classFieldDef.classFieldName.value,
    hide = true
)
