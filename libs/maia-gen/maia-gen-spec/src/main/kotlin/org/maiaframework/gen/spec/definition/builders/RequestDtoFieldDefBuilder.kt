package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.DatabaseIndexDef
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.RequestDtoFieldDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
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
import org.maiaframework.gen.spec.definition.lang.FieldTypes
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
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.LengthConstraintDef
import org.maiaframework.gen.spec.definition.validation.MaxConstraintDef
import org.maiaframework.gen.spec.definition.validation.MinConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef


@MaiaDslMarker
class RequestDtoFieldDefBuilder private constructor(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val typeaheadDef: TypeaheadDef?
) {


    private val classFieldDefBuilder = ClassFieldDefBuilder(classFieldName, fieldType)


    init {

        this.classFieldDefBuilder.typeaheadDef = typeaheadDef

    }


    private var databaseIndexDef: DatabaseIndexDef? = null


    private var otherFieldDefBuilder: OtherFieldDefBuilder? = null


    constructor(
        classFieldName: ClassFieldName,
        enumDef: EnumDef
    ) : this(
        classFieldName = classFieldName,
        fieldType = FieldTypes.enum(enumDef),
        typeaheadDef = null,
    )


    constructor(
        classFieldName: ClassFieldName,
        stringTypeDef: StringTypeDef
    ) : this(
        classFieldName = classFieldName,
        fieldType = FieldTypes.stringType(stringTypeDef),
        typeaheadDef = null,
    )


    constructor(
        classFieldName: ClassFieldName,
        requestDtoDef: RequestDtoDef
    ) : this(
        classFieldName = classFieldName,
        fieldType = FieldTypes.requestDto(requestDtoDef),
        typeaheadDef = null,
    )


    constructor(
        classFieldName: ClassFieldName,
        fieldType: FieldType
    ) : this(
        classFieldName = classFieldName,
        fieldType = fieldType,
        typeaheadDef = null,
    )


    fun nullable(): RequestDtoFieldDefBuilder {

        this.classFieldDefBuilder.removeValidationConstraint(NotNullConstraintDef::class)
        this.classFieldDefBuilder.removeValidationConstraint(NotBlankConstraintDef::class)
        this.classFieldDefBuilder.nullability = Nullability.NULLABLE
        return this

    }


    fun description(description: String): RequestDtoFieldDefBuilder {

        this.classFieldDefBuilder.description = Description(description)
        return this

    }


    fun fieldDisplayName(fieldDisplayName: String): RequestDtoFieldDefBuilder {

        this.classFieldDefBuilder.fieldDisplayName = FieldDisplayName(fieldDisplayName)
        return this

    }


    fun withOtherLinkedField(
        init: (OtherFieldDefBuilder.() -> Unit)? = null
    ) {

        `confirm the field type supports an Other field`()

        val builder = OtherFieldDefBuilder()
        init?.invoke(builder)
        this.otherFieldDefBuilder = builder

    }


    fun formPlaceholderText(formPlaceholderText: String): RequestDtoFieldDefBuilder {

        this.classFieldDefBuilder.formPlaceholderText = FormPlaceholderText(formPlaceholderText)
        return this

    }


    fun withEmailConstraint(): RequestDtoFieldDefBuilder {

        addValidationConstraint(EmailConstraintDef.INSTANCE)
        return this

    }


    fun withUrlConstraint(): RequestDtoFieldDefBuilder {

        addValidationConstraint(UrlConstraintDef.INSTANCE)
        return this

    }


    fun withMinSize(min: Long): RequestDtoFieldDefBuilder {

        addValidationConstraint(MinConstraintDef.of(min))
        return this

    }


    fun withMaxSize(max: Long): RequestDtoFieldDefBuilder {

        addValidationConstraint(MaxConstraintDef.of(max))
        return this

    }


    fun withLength(min: Long? = null, max: Long? = null): RequestDtoFieldDefBuilder {

        addValidationConstraint(LengthConstraintDef.of(min, max))
        return this

    }


    fun withValidationConstraints(validationConstraints: Collection<AbstractValidationConstraintDef>): RequestDtoFieldDefBuilder {

        validationConstraints.forEach { addValidationConstraint(it) }
        return this

    }


    fun withDatabaseIndexDef(databaseIndexDef: DatabaseIndexDef?): RequestDtoFieldDefBuilder {

        this.databaseIndexDef = databaseIndexDef
        return this

    }


    fun build(): List<RequestDtoFieldDef> {

        val classFieldDef = this.classFieldDefBuilder.build()
        val requestDtoFieldDef = RequestDtoFieldDef(classFieldDef, this.databaseIndexDef)

        val otherClassFieldDef = this.otherFieldDefBuilder?.let { builder ->
            ClassFieldDef(
                classFieldName = this.classFieldName.withSuffix("Other"),
                fieldType = FieldTypes.string,
                fieldLinkedTo = classFieldDef,
                isModifiableBySystem = false,
                nullability = Nullability.NULLABLE,
                displayName = builder.displayName?.let { FieldDisplayName(it) } ?: this.classFieldDefBuilder.fieldDisplayName?.let { FieldDisplayName("Other $it") },
                description = builder.description?.let { Description(it) },
                formPlaceholderText = builder.formPlaceholderText?.let { FormPlaceholderText(it) },
                textCase = builder.textCase,
                providedValidationConstraints = builder.buildValidationConstraints()
            )

        }

        return listOf(
            requestDtoFieldDef,
            otherClassFieldDef?.let { RequestDtoFieldDef(it, databaseIndexDef = null) }
        ).filterNotNull()

    }


    private fun addValidationConstraint(validationConstraint: AbstractValidationConstraintDef) {

        this.classFieldDefBuilder.addValidationConstraint(validationConstraint)
        this.classFieldDefBuilder.addAnnotationDef(validationConstraint.associatedAnnotationDef)

    }


    fun masked(): RequestDtoFieldDefBuilder {

        this.classFieldDefBuilder.isMasked = true
        return this

    }


    fun unique(): RequestDtoFieldDefBuilder {

        this.classFieldDefBuilder.unique = true
        return this

    }


    private fun `confirm the field type supports an Other field`() {

        when (fieldType) {
            is BooleanFieldType -> `throw Other field not supported`(fieldType)
            is BooleanTypeFieldType -> `throw Other field not supported`(fieldType)
            is BooleanValueClassFieldType -> `throw Other field not supported`(fieldType)
            is DataClassFieldType -> `throw Other field not supported`(fieldType)
            is DomainIdFieldType -> `throw Other field not supported`(fieldType)
            is DoubleFieldType -> `throw Other field not supported`(fieldType)
            is EnumFieldType -> return
            is EsDocFieldType -> `throw Other field not supported`(fieldType)
            is ForeignKeyFieldType -> `throw Other field not supported`(fieldType)
            is FqcnFieldType -> `throw Other field not supported`(fieldType)
            is IdAndNameFieldType -> `throw Other field not supported`(fieldType)
            is InstantFieldType -> `throw Other field not supported`(fieldType)
            is IntFieldType -> `throw Other field not supported`(fieldType)
            is IntTypeFieldType -> `throw Other field not supported`(fieldType)
            is IntValueClassFieldType -> `throw Other field not supported`(fieldType)
            is ListFieldType -> `throw Other field not supported`(fieldType)
            is LocalDateFieldType -> `throw Other field not supported`(fieldType)
            is LongFieldType -> `throw Other field not supported`(fieldType)
            is LongTypeFieldType -> `throw Other field not supported`(fieldType)
            is MapFieldType -> `throw Other field not supported`(fieldType)
            is ObjectIdFieldType -> `throw Other field not supported`(fieldType)
            is PeriodFieldType -> `throw Other field not supported`(fieldType)
            is RequestDtoFieldType -> `throw Other field not supported`(fieldType)
            is SetFieldType -> `throw Other field not supported`(fieldType)
            is SimpleResponseDtoFieldType -> `throw Other field not supported`(fieldType)
            is StringFieldType -> `throw Other field not supported`(fieldType)
            is StringTypeFieldType -> `throw Other field not supported`(fieldType)
            is StringValueClassFieldType -> `throw Other field not supported`(fieldType)
            is UrlFieldType -> `throw Other field not supported`(fieldType)
        }

    }


    private fun `throw Other field not supported`(fieldType: FieldType) {

        throw IllegalArgumentException("A field type of $fieldType does not support an Other field")

    }


}
