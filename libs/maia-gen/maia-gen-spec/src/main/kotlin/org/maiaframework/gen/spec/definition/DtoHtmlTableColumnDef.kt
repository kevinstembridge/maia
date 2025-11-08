package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.AnnotationUsageSite
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import java.util.SortedSet


class DtoHtmlTableColumnDef(
    val fieldPathInSourceData: String,
    val dtoFieldName: String,
    columnHeader: String?,
    val isSortable: Boolean,
    val isFilterable: Boolean,
    val fieldType: FieldType,
    val nullability: Nullability,
    providedAgGridCellDataType: AgGridCellDataType?,
    cellRenderer: AgGridCellRendererDef?,
    val pipes: List<String>
) : AbstractDtoHtmlTableColumnDef(
    columnHeader,
    cellRenderer
), Comparable<DtoHtmlTableColumnDef> {


    val filterModelFieldType = determineFilterModelFieldType()


    val filterModelFilterType = determineFilterModelFilterType()


    val agGridCellDateType = providedAgGridCellDataType ?: determineAgGridCellDataType()


    private fun determineFilterModelFieldType(): String {

        return when (fieldType) {
            is BooleanFieldType -> "boolean"
            is BooleanTypeFieldType -> "boolean"
            is BooleanValueClassFieldType -> "boolean"
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> "id"
            is DoubleFieldType -> "number"
            is EnumFieldType -> "text"
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> "id"
            is FqcnFieldType -> TODO("YAGNI?")
            is IdAndNameFieldType -> "id"
            is InstantFieldType -> "date"
            is IntFieldType -> "number"
            is IntTypeFieldType -> "number"
            is IntValueClassFieldType -> "number"
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> "date"
            is LongFieldType -> "number"
            is LongTypeFieldType -> "number"
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> "id"
            is PeriodFieldType -> "text"
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> "text"
            is StringTypeFieldType -> "text"
            is StringValueClassFieldType -> "text"
            is UrlFieldType -> "text"
        }

    }


    private fun determineFilterModelFilterType(): String {

        return when (fieldType) {
            is BooleanFieldType -> "equals"
            is BooleanTypeFieldType -> "equals"
            is BooleanValueClassFieldType -> "equals"
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> "equals"
            is DoubleFieldType -> "equals"
            is EnumFieldType -> "equals"
            is EsDocFieldType -> TODO("YAGNI?")
            is FqcnFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> "equals"
            is IdAndNameFieldType -> "equals"
            is InstantFieldType -> "equals"
            is IntFieldType -> "equals"
            is IntTypeFieldType -> "equals"
            is IntValueClassFieldType -> "equals"
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> "equals"
            is LongFieldType -> "equals"
            is LongTypeFieldType -> "equals"
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> "equals"
            is PeriodFieldType -> "equals"
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> "contains"
            is StringTypeFieldType -> "contains"
            is StringValueClassFieldType -> "contains"
            is UrlFieldType -> "equals"
        }

    }


    private fun determineAgGridCellDataType(): AgGridCellDataType {

        return when (fieldType) {
            is BooleanFieldType -> AgGridCellDataType.boolean
            is BooleanTypeFieldType -> AgGridCellDataType.boolean
            is BooleanValueClassFieldType -> AgGridCellDataType.boolean
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> AgGridCellDataType.text
            is DoubleFieldType -> AgGridCellDataType.number
            is EnumFieldType -> AgGridCellDataType.text
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> AgGridCellDataType.text
            is FqcnFieldType -> TODO("YAGNI?")
            is IdAndNameFieldType -> AgGridCellDataType.date
            is InstantFieldType -> AgGridCellDataType.text
            is IntFieldType -> AgGridCellDataType.number
            is IntTypeFieldType -> AgGridCellDataType.number
            is IntValueClassFieldType -> AgGridCellDataType.number
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> AgGridCellDataType.dateString
            is LongFieldType -> AgGridCellDataType.number
            is LongTypeFieldType -> AgGridCellDataType.number
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> AgGridCellDataType.text
            is PeriodFieldType -> AgGridCellDataType.text
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> AgGridCellDataType.text
            is StringTypeFieldType -> AgGridCellDataType.text
            is StringValueClassFieldType -> AgGridCellDataType.text
            is UrlFieldType -> AgGridCellDataType.text
        }

    }


    override fun compareTo(other: DtoHtmlTableColumnDef): Int {

        return this.dtoFieldName.compareTo(other.dtoFieldName)

    }


    private val enhancedAnnotationDefs = enhanceAnnotationDefs()


    private fun enhanceAnnotationDefs(): SortedSet<AnnotationDef> {

        val fieldNameChars = this.dtoFieldName.toCharArray()

        return if (fieldNameChars.size > 1 && fieldNameChars[0].isLowerCase() && fieldNameChars[1].isUpperCase()) {
            sortedSetOf(AnnotationDef(Fqcns.JACKSON_JSON_PROPERTY, dtoFieldName, usageSite = AnnotationUsageSite.get))
        } else {
            sortedSetOf()
        }

    }


    val classFieldDef = aClassField(this.dtoFieldName, fieldType = fieldType) {
        nullability(nullability)
        addAnnotations(enhancedAnnotationDefs)
    }.build()


    enum class AgGridCellDataType {

        text,
        number,
        boolean,
        date,
        dateString

    }

}
