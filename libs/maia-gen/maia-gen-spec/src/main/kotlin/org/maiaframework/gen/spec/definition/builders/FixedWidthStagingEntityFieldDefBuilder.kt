package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.DataRowHeaderName
import org.maiaframework.gen.spec.definition.DataRowStagingEntityFieldDef
import org.maiaframework.gen.spec.definition.DateTimeFormatterConstant
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.SimpleFieldDef
import org.maiaframework.gen.spec.definition.flags.IsCreatableByUser
import org.maiaframework.gen.spec.definition.flags.IsDeltaField
import org.maiaframework.gen.spec.definition.flags.IsDeltaKey
import org.maiaframework.gen.spec.definition.flags.IsDerived
import org.maiaframework.gen.spec.definition.flags.IsPrimaryKey
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
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
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.LengthConstraintDef


@MaiaDslMarker
class FixedWidthStagingEntityFieldDefBuilder(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val dataRowHeaderName: DataRowHeaderName,
    private val fixedWidth: Int,
    private val entityBaseName: EntityBaseName,
    private val packageName: PackageName,
    private val expectedFieldDef: SimpleFieldDef
) {


    private var tableColumnName = TableColumnName(classFieldName.toSnakeCase())


    private var nullability = Nullability.NULLABLE


    private var unique = false


    private val validationConstraints = mutableSetOf<AbstractValidationConstraintDef>()


    private var description: Description? = null


    private var fieldDisplayName: FieldDisplayName? = null


    private var dateTimeFormatterConstant: DateTimeFormatterConstant? = null


    fun unique() {

        this.unique = true

    }


    fun description(description: String) {

        this.description = Description(description)

    }


    fun build(): DataRowStagingEntityFieldDef {

        when (fieldType) {
            is BooleanFieldType -> doNothing()
            is BooleanTypeFieldType -> doNothing()
            is BooleanValueClassFieldType -> doNothing()
            is DataClassFieldType -> doNothing()
            is DomainIdFieldType -> doNothing()
            is DoubleFieldType -> doNothing()
            is EnumFieldType -> addMaxLengthConstraint()
            is EsDocFieldType -> doNothing()
            is ForeignKeyFieldType -> doNothing()
            is FqcnFieldType -> doNothing()
            is IdAndNameFieldType -> doNothing()
            is InstantFieldType -> doNothing()
            is IntFieldType -> doNothing()
            is IntTypeFieldType -> doNothing()
            is IntValueClassFieldType -> doNothing()
            is ListFieldType -> doNothing()
            is LocalDateFieldType -> doNothing()
            is LongFieldType -> doNothing()
            is LongTypeFieldType -> doNothing()
            is MapFieldType -> doNothing()
            is ObjectIdFieldType -> doNothing()
            is PeriodFieldType -> addMaxLengthConstraint()
            is RequestDtoFieldType -> doNothing()
            is SetFieldType -> doNothing()
            is SimpleResponseDtoFieldType -> doNothing()
            is StringFieldType -> addMaxLengthConstraint()
            is StringTypeFieldType -> addMaxLengthConstraint()
            is StringValueClassFieldType -> doNothing()
            is UrlFieldType -> doNothing()
        }

        val classFieldDef = ClassFieldDef(
            classFieldName = classFieldName,
            description = description,
            fieldType = fieldType,
            nullability = nullability,
            isUnique = this.unique,
            displayName = this.fieldDisplayName,
            providedValidationConstraints = validationConstraints.toSortedSet()
        )

        val entityFieldDef = EntityFieldDef(
            this.entityBaseName,
            this.packageName,
            classFieldDef,
            this.tableColumnName,
            withExistsEndpoint = false,
            IsDeltaKey.FALSE,
            IsPrimaryKey.FALSE,
            IsDeltaField.FALSE,
            IsDerived.FALSE,
            isCreatableByUser = IsCreatableByUser.FALSE
        )

        return DataRowStagingEntityFieldDef(
            entityFieldDef,
            this.dataRowHeaderName,
            this.fixedWidth,
            this.expectedFieldDef,
            this.dateTimeFormatterConstant
        )

    }


    private fun doNothing() {
        // Do nothing
    }

    private fun addMaxLengthConstraint() {
        this.validationConstraints.add(LengthConstraintDef.of(max = fixedWidth.toLong()))
    }


    fun tableColumnName(tableColumnName: String) {

        this.tableColumnName = TableColumnName(tableColumnName)

    }


    fun dateTimeFormatter(formatterConstant: DateTimeFormatterConstant) {

        this.dateTimeFormatterConstant = formatterConstant


    }


    fun fieldDisplayName(displayName: String) {

        this.fieldDisplayName = FieldDisplayName(displayName)

    }


}
