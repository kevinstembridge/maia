package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.AnnotationUsageSite
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability
import java.util.SortedSet


class SimpleResponseDtoFieldDef(
    val classFieldName: ClassFieldName,
    fieldType: FieldType,
    nullability: Nullability,
    isMasked: Boolean,
    val caseSensitive: CaseSensitive
) : Comparable<SimpleResponseDtoFieldDef> {


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


    override fun compareTo(other: SimpleResponseDtoFieldDef): Int {

        return this.classFieldDef.compareTo(other.classFieldDef)

    }


}
