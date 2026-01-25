package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class EsDocFieldDef(
    val classFieldDef: ClassFieldDef,
    val mappingType: EsDocMappingType,
    val entityFieldDef: EntityFieldDef?
)
