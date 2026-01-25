package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.Uqcn

class RowMapperDef(
    val uqcn: Uqcn,
    val fieldDefs: List<RowMapperFieldDef>,
    val classDef: ClassDef,
    val isForEditDto: Boolean
)
