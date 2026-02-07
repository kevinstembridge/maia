package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.MaiaGenConstants
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Uqcn


class FormValidationResponseDtoRenderer() : TypescriptInterfaceDtoRenderer(
    renderedFilePath = MaiaGenConstants.FORM_VALIDATION_RESPONSE_DTO_RENDERED_FILE_PATH,
    className = Uqcn("FormValidationResponseDto"),
    fields = listOf(
        aClassField("invalid", FieldTypes.boolean).build(),
        aClassField("message", FieldTypes.string).build(),
    ),
    dtoCharacteristics = setOf(DtoCharacteristic.RESPONSE_DTO)
)
