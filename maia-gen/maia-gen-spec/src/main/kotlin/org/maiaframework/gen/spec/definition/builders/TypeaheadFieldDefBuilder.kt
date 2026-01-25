package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.TypeaheadFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability


@MaiaDslMarker
class TypeaheadFieldDefBuilder(
    private val dtoFieldName: String,
    private val fieldType: FieldType,
    private val entityFieldDef: EntityFieldDef?
) {


    var esDocMappingType: EsDocMappingType = EsDocMappingTypes.text


    var nullable: Boolean = false


    fun build(): TypeaheadFieldDef {

        val nullability = Nullability.of(nullable)
        val classFieldDef = entityFieldDef?.classFieldDef?.withFieldName(dtoFieldName)?.withFieldType(fieldType)?.copy(nullability = nullability)
            ?: aClassField(dtoFieldName, fieldType).nullability(nullability).build()

        return TypeaheadFieldDef(
            this.entityFieldDef,
            this.esDocMappingType,
            classFieldDef
        )

    }


}
