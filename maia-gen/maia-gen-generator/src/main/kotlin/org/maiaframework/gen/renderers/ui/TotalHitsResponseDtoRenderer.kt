package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.EnumDefs
import org.maiaframework.gen.spec.definition.MaiaGenConstants
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Uqcn


class TotalHitsResponseDtoRenderer : TypescriptInterfaceDtoRenderer(
    renderedFilePath = MaiaGenConstants.TOTAL_HITS_RENDERED_FILE_PATH,
    className = Uqcn("TotalHits"),
    fields = listOf(
        aClassField("count", FieldTypes.int).build(),
        aClassField("relation", FieldTypes.enum(EnumDefs.TOTAL_HITS_RELATION_ENUM_DEF)).build(),
    ),
    dtoCharacteristics = setOf(DtoCharacteristic.RESPONSE_DTO)
)
