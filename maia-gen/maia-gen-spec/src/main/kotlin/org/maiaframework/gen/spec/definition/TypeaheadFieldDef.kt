package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

data class TypeaheadFieldDef(
    val classFieldDef: ClassFieldDef,
    val isIdField: Boolean,
    val esDocMappingType: EsDocMappingType,
    val entityFieldDef: EntityFieldDef? = null
) {


    val fieldName = classFieldDef.classFieldName


    val fieldType = classFieldDef.fieldType


}
