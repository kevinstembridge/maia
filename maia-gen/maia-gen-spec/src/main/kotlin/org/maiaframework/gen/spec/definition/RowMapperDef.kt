package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.Uqcn

class RowMapperDef(
    val rowUqcn: Uqcn,
    val fieldDefs: List<RowMapperFieldDef>,
    val classDef: ClassDef,
    val isForEditDto: Boolean
) {


    val entityFieldDefs: List<EntityFieldRowMapperFieldDef> = fieldDefs.filterIsInstance<EntityFieldRowMapperFieldDef>()


    val manyToManyFieldDefs: List<ManyToManyRowMapperFieldDef> = fieldDefs.filterIsInstance<ManyToManyRowMapperFieldDef>()


}
