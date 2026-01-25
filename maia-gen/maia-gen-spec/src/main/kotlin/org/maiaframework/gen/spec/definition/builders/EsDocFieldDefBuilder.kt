package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EsDocFieldDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability


@MaiaDslMarker
class EsDocFieldDefBuilder(
    private val fieldName: ClassFieldName,
    private val fieldType: FieldType
) {


    private var nullability = Nullability.NOT_NULLABLE


    private var mappingType = fieldType.elasticMappingType ?: EsDocMappingTypes.text


    fun build(): EsDocFieldDef {

        val classFieldDef = ClassFieldDef(
            classFieldName = this.fieldName,
            fieldType = this.fieldType,
            nullability = this.nullability
        )

        return EsDocFieldDef(classFieldDef, this.mappingType, entityFieldDef = null)

    }


    fun nullable() {

        this.nullability = Nullability.NULLABLE

    }


    fun mappingType(mappingType: EsDocMappingType) {

        this.mappingType = mappingType

    }


}
