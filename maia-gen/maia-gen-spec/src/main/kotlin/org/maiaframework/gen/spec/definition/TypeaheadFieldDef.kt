package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

data class TypeaheadFieldDef(
    val entityFieldDef: EntityFieldDef? = null,
    val esDocMappingType: EsDocMappingType,
    val classFieldDef: ClassFieldDef
) {


    val fieldName = classFieldDef.classFieldName


    val fieldType = classFieldDef.fieldType


}
