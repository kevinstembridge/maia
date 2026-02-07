package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.MaiaGenConstants
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Uqcn


class ForeignKeyReferencesExistResponseDtoRenderer() : TypescriptInterfaceDtoRenderer(
    renderedFilePath = MaiaGenConstants.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_RENDERED_FILE_PATH,
    className = Uqcn("ForeignKeyReferencesExistResponseDto"),
    fields = listOf(
        aClassField("exists", FieldTypes.boolean).build(),
        aClassField("entityKey", FieldTypes.string).nullable().build(),
    ),
    dtoCharacteristics = setOf(DtoCharacteristic.RESPONSE_DTO)
)
