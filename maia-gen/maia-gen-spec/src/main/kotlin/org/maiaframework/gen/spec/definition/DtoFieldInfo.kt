package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


data class DtoFieldInfo(
    val classFieldDef: ClassFieldDef,
    val displayName: FieldDisplayName?
)
