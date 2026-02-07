package org.maiaframework.gen.spec.definition

object EnumDefs {


    val CHANGE_TYPE_ENUM_DEF = EnumDef(
        fqcn = Fqcns.MAIA_CHANGE_TYPE,
        enumValueDefs = emptyList(),
        isProvided = true,
        withTypescript = false,
        withEnumSelectionOptions = false
    )


    val LIFECYCLE_STATE_ENUM_DEF = EnumDef(
        fqcn = Fqcns.MAIA_LIFECYCLE_STATE,
        enumValueDefs = emptyList(),
        isProvided = true,
        withTypescript = true,
        withEnumSelectionOptions = false
    )


    val TOTAL_HITS_RELATION_ENUM_DEF = EnumDef(
        fqcn = Fqcns.TOTAL_HITS_RELATION,
        enumValueDefs = listOf(
            EnumValueDef("eq"),
            EnumValueDef("gte")
        ),
        isProvided = false,
        withTypescript = true,
        withEnumSelectionOptions = false
    )


}
