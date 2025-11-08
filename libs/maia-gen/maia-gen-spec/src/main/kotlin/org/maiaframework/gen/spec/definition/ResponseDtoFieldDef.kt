package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.jdbc.DbColumnFieldDef
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.AnnotationUsageSite
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import java.util.SortedSet


class ResponseDtoFieldDef(
    val classFieldName: ClassFieldName,
    tableColumnName: TableColumnName,
    fieldType: FieldType,
    val nullability: Nullability,
    isMasked: Boolean,
    val caseSensitive: CaseSensitive,
    fieldReaderParameterizedType: ParameterizedType?,
    fieldWriterParameterizedType: ParameterizedType?
) : Comparable<ResponseDtoFieldDef> {


    private val enhancedAnnotationDefs = enhanceAnnotationDefs()

    private fun enhanceAnnotationDefs(): SortedSet<AnnotationDef> {

        val fieldNameChars = this.classFieldName.value.toCharArray()

        if (fieldNameChars.size > 1 && fieldNameChars[0].isLowerCase() && fieldNameChars[1].isUpperCase()) {
            return sortedSetOf(AnnotationDef(Fqcns.JACKSON_JSON_PROPERTY, classFieldName.value, usageSite = AnnotationUsageSite.get))
        } else {
            return sortedSetOf()
        }

    }


    val classFieldDef: ClassFieldDef = ClassFieldDef(
        classFieldName,
        fieldType = fieldType,
        nullability = nullability,
        isMasked = isMasked,
        annotationDefs = enhancedAnnotationDefs
    )


    val collectionFieldDef = DbColumnFieldDef(tableColumnName, fieldReaderParameterizedType, fieldWriterParameterizedType)


    val fieldReaderClassField: ClassFieldDef? = this.collectionFieldDef.fieldReaderClassField(classFieldName)


    val fieldWriterClassField: ClassFieldDef? = this.collectionFieldDef.fieldWriterClassField(classFieldName)


    override fun compareTo(other: ResponseDtoFieldDef): Int {

        return this.classFieldDef.compareTo(other.classFieldDef)

    }


    fun copyWithFieldName(fieldName: String): ResponseDtoFieldDef {

        return ResponseDtoFieldDef(
            ClassFieldName(fieldName),
            collectionFieldDef.tableColumnName,
            classFieldDef.fieldType,
            classFieldDef.nullability,
            classFieldDef.isMasked,
            caseSensitive,
            collectionFieldDef.fieldReaderParameterizedType,
            collectionFieldDef.fieldWriterParameterizedType
        )

    }


}
