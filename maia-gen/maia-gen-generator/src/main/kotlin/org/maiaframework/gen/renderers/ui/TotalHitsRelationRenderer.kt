package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EnumValueDef
import org.maiaframework.gen.spec.definition.Fqcns


class TotalHitsRelationRenderer : EnumTypescriptRenderer(
    EnumDef(
        fqcn = Fqcns.TOTAL_HITS_RELATION,
        enumValueDefs = listOf(
            EnumValueDef("eq"),
            EnumValueDef("gte")
        ),
        isProvided = false,
        withTypescript = true,
        withEnumSelectionOptions = false
    )
)
