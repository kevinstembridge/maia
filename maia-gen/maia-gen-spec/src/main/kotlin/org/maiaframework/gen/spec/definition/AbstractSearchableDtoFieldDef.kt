package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


abstract class AbstractSearchableDtoFieldDef(
    val classFieldDef: ClassFieldDef,
    val sortIndexAndDirection: SortIndexAndDirection?
) {


    val classFieldName = classFieldDef.classFieldName


    val fieldSortModel: FieldSortModel? = sortIndexAndDirection?.let { FieldSortModel(classFieldDef.classFieldName, it) }


    open val displayName = classFieldDef.displayName


    abstract fun copyWithFieldName(dtoFieldName: String): AbstractSearchableDtoFieldDef


}
