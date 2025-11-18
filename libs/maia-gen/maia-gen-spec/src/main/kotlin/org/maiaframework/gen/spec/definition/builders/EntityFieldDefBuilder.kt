package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.ForeignKeyFieldDef
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.flags.IsCreatableByUser
import org.maiaframework.gen.spec.definition.flags.IsDeltaField
import org.maiaframework.gen.spec.definition.flags.IsDeltaKey
import org.maiaframework.gen.spec.definition.flags.IsDerived
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.IsPrimaryKey
import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.FieldTypes.canHaveLengthConstraint
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isNumeric
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.LengthConstraintDef
import org.maiaframework.gen.spec.definition.validation.MaxConstraintDef
import org.maiaframework.gen.spec.definition.validation.MinConstraintDef
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef


@MaiaDslMarker
class EntityFieldDefBuilder(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val entityBaseName: EntityBaseName,
    private val packageName: PackageName,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {


    private val classFieldDefBuilder = ClassFieldDefBuilder(
        classFieldName,
        fieldType
    )


    private var tableColumnName = TableColumnName(classFieldName.toSnakeCase())


    private var derived = IsDerived.FALSE


    private var isCreatableByUser = IsCreatableByUser.TRUE


    private var withExistsEndpoint = false


    private var fieldReaderParameterizedType: ParameterizedType? = null


    private var fieldWriterParameterizedType: ParameterizedType? = null


    private var isDeltaKey: IsDeltaKey = IsDeltaKey.FALSE


    private var isPrimaryKey: IsPrimaryKey = IsPrimaryKey.FALSE


    private var isDeltaField: IsDeltaField = IsDeltaField.TRUE


    private val fieldReaderClassName: ParameterizedType?
        get() = if (this.fieldReaderParameterizedType != null) {
            this.fieldReaderParameterizedType
        } else
            this.defaultFieldTypeFieldReaderProvider(this.fieldType)


    private val fieldWriterClassName: ParameterizedType?
        get() = if (this.fieldWriterParameterizedType != null) {
            this.fieldWriterParameterizedType
        } else
            this.defaultFieldTypeFieldWriterProvider(this.fieldType)


    constructor(
        classFieldName: ClassFieldName,
        foreignKeyFieldDef: ForeignKeyFieldDef,
        entityBaseName: EntityBaseName,
        packageName: PackageName,
        defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
        defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
    ) : this(
        classFieldName,
        FieldTypes.foreignKey(foreignKeyFieldDef),
        entityBaseName,
        packageName,
        defaultFieldTypeFieldReaderProvider,
        defaultFieldTypeFieldWriterProvider
    )


    fun modifiableBySystem() {

        this.classFieldDefBuilder.modifiableBySystem = true

    }


    fun editableByUser() {

        this.classFieldDefBuilder.isEditableByUser = IsEditableByUser.TRUE

    }


    fun nullable() {

        this.classFieldDefBuilder.nullability = Nullability.NULLABLE

    }


    fun unique(withExistsEndpoint: Boolean = false) {

        this.classFieldDefBuilder.unique = true
        this.withExistsEndpoint = withExistsEndpoint

    }


    fun description(description: String) {

        this.classFieldDefBuilder.description = Description(description)

    }


    fun valueMappings(valueMappings: Map<String, String>) {

        this.classFieldDefBuilder.valueMappings = valueMappings

    }


    fun build(): EntityFieldDef {

        val fieldReaderClassName = fieldReaderClassName
        val fieldWriterClassName = fieldWriterClassName

        val classFieldDef = this.classFieldDefBuilder.build()

        return EntityFieldDef(
            this.entityBaseName,
            this.packageName,
            classFieldDef,
            this.tableColumnName,
            this.withExistsEndpoint,
            this.isDeltaKey,
            this.isPrimaryKey,
            this.isDeltaField,
            this.derived,
            this.isCreatableByUser,
            fieldReaderClassName,
            fieldWriterClassName
        )

    }


    private fun addValidationConstraint(validationConstraint: AbstractValidationConstraintDef) {

        this.classFieldDefBuilder.addValidationConstraint(validationConstraint)

    }


    fun tableColumnName(tableColumnName: String) {

        this.tableColumnName = TableColumnName(tableColumnName)

    }


    fun notCreatableByUser() {

        this.isCreatableByUser = IsCreatableByUser.FALSE

    }


    fun fieldReader(fieldReaderFqcn: Fqcn) {

        fieldReader(ParameterizedType(fieldReaderFqcn))

    }


    fun fieldReader(fieldReaderParameterizedType: ParameterizedType) {

        this.fieldReaderParameterizedType = fieldReaderParameterizedType

    }


    fun fieldWriter(fieldReaderFqcn: Fqcn) {

        fieldWriter(ParameterizedType(fieldReaderFqcn))

    }


    fun fieldWriter(fieldWriterParameterizedType: ParameterizedType) {

        this.fieldWriterParameterizedType = fieldWriterParameterizedType

    }


    fun masked() {

        this.classFieldDefBuilder.isMasked = true

    }


    fun lengthConstraint(min: Long? = null, max: Long? = null) {

        if (this.fieldType.canHaveLengthConstraint() == false) {
            throw RuntimeException("Cannot specify a lengthConstraint for a non-string field '${this.classFieldName}' of type $fieldType on entity '${this.entityBaseName}'.")
        }

        addValidationConstraint(LengthConstraintDef.of(min, max))

    }


    fun minConstraint(min: Long) {

        if (this.fieldType.isNumeric() == false) {
            throw RuntimeException("Cannot specify a minConstraint for a non-numeric field '${this.classFieldName}' of type $fieldType on entity '${this.entityBaseName}'.")
        }

        addValidationConstraint(MinConstraintDef.of(min))

    }


    fun maxConstraint(max: Long) {

        if (this.fieldType.isNumeric() == false) {
            throw RuntimeException("Cannot specify a maxConstraint for a non-numeric field [${this.classFieldName}] of type $fieldType on entity '${this.entityBaseName}'.")
        }

        addValidationConstraint(MaxConstraintDef.of(max))

    }


    fun withEmailConstraint() {

        addValidationConstraint(EmailConstraintDef.INSTANCE)
        addValidationConstraint(LengthConstraintDef.of(min = 5, max = 500))

    }


    fun withUrlConstraint() {

        addValidationConstraint(UrlConstraintDef.INSTANCE)
        addValidationConstraint(LengthConstraintDef.of(min = 5, max = 500))

    }


    fun deltaKey() {

        this.isDeltaKey = IsDeltaKey.TRUE
        this.isDeltaField = IsDeltaField.FALSE

    }


    fun nonDeltaField() {

        this.isDeltaField = IsDeltaField.FALSE

    }


    fun fieldDisplayName(displayName: String) {

        this.classFieldDefBuilder.fieldDisplayName = FieldDisplayName(displayName)

    }


    fun typeaheadField(typeaheadDef: TypeaheadDef) {

        this.classFieldDefBuilder.typeaheadDef = typeaheadDef

    }


    fun formPlaceholderText(formPlaceholderText: String) {

        this.classFieldDefBuilder.formPlaceholderText = FormPlaceholderText(formPlaceholderText)

    }


    fun forceToUpperCase() {

        this.classFieldDefBuilder.textCase = TextCase.UPPER

    }


    fun forceToLowerCase() {

        this.classFieldDefBuilder.textCase = TextCase.LOWER

    }


    fun derived() {

        this.derived = IsDerived.TRUE
        notCreatableByUser()

    }


    fun primaryKey() {

        this.isPrimaryKey = IsPrimaryKey(value = true, isSurrogate = false)

    }


}
