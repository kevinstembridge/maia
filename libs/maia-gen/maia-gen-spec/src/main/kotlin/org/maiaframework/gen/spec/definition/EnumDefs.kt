package org.maiaframework.gen.spec.definition

object EnumDefs {


    val CHANGE_TYPE_ENUM_DEF = EnumDef(
        Fqcns.MAIA_CHANGE_TYPE,
        emptyList(),
        true,
        withTypescript = false,
        withEnumSelectionOptions = false
    )


    val LIFECYCLE_STATE_ENUM_DEF = EnumDef(
        Fqcns.MAIA_LIFECYCLE_STATE,
        emptyList(),
        true,
        withTypescript = true,
        withEnumSelectionOptions = false
    )


}
