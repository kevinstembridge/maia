package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldTypes.isStringBased
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.Nullability

data class DataRowStagingEntityFieldDef(
    val entityFieldDef: EntityFieldDef,
    val dataRowHeaderName: DataRowHeaderName,
    val width: Int,
    val expectedFieldDef: SimpleFieldDef,
    val dateTimeFormatterConstant: DateTimeFormatterConstant? = null
) {


    val classFieldName = entityFieldDef.classFieldDef.classFieldName


    val tableColumnName = entityFieldDef.tableColumnName


    val isString = expectedFieldDef.fieldType.isStringBased()


    val isInt = expectedFieldDef.fieldType is IntFieldType


    val isLocalDate = expectedFieldDef.fieldType is LocalDateFieldType


    val isNullableString = expectedFieldDef.nullability == Nullability.NULLABLE && isString


    val nullability = expectedFieldDef.nullability


    val isNullable = nullability == Nullability.NULLABLE


    val isNotNullable = nullability == Nullability.NOT_NULLABLE


}
