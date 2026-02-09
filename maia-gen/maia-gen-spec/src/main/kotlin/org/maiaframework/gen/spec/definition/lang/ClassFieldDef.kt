package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.ForeignKeyFieldDef
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isNumeric
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isStringBased
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.EnumConstraintDef
import org.maiaframework.gen.spec.definition.validation.LengthConstraintDef
import org.maiaframework.gen.spec.definition.validation.MaxConstraintDef
import org.maiaframework.gen.spec.definition.validation.MinConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef
import java.util.SortedSet

data class ClassFieldDef(
    val classFieldName: ClassFieldName,
    val description: Description? = null,
    val fieldType: FieldType,
    val isModifiableBySystem: Boolean = false,
    val isEditableByUser: IsEditableByUser = IsEditableByUser.FALSE,
    val nullability: Nullability = Nullability.NOT_NULLABLE,
    val isMasked: Boolean = false,
    val isPrivateProperty: Boolean = false,
    val isConstructorOnly: Boolean = false,
    val isUnique: Boolean = false,
    val typeaheadDef: TypeaheadDef? = null,
    val displayName: FieldDisplayName? = null,
    val formPlaceholderText: FormPlaceholderText? = null,
    val textCase: TextCase = TextCase.ORIGINAL,
    val annotationDefs: SortedSet<AnnotationDef> = sortedSetOf(),
    val pipes: List<String> = emptyList(),
    private val providedValidationConstraints: SortedSet<AbstractValidationConstraintDef> = sortedSetOf(),
    val valueMappings: Map<String, String>? = null,
    val fieldLinkedTo: ClassFieldDef? = null,
    val providedDefaultFormFieldValue: String? = null
) : Comparable<ClassFieldDef> {


    val fqcn = fieldType.fqcn


    val nullable: Boolean = nullability.nullable


    val validationConstraints = enrichWithImplicitValidationConstraints(providedValidationConstraints)


    val unqualifiedToString: String


    val validationAnnotations: List<AnnotationDef>
        get() = enrichWithImplicitValidationConstraints(validationConstraints).map { it.associatedAnnotationDef }


    val foreignKeyFieldDef = if (fieldType is ForeignKeyFieldType) fieldType.foreignKeyFieldDef else null


    val isList = this.fieldType is ListFieldType


    val isSet = this.fieldType is SetFieldType


    val isMap = this.fieldType is MapFieldType


    val isStringValueClass = this.fieldType is StringValueClassFieldType


    val isBooleanValueClass = this.fieldType is BooleanValueClassFieldType


    val isValueClass = isStringValueClass || isBooleanValueClass


    val isListSetOrMap = isList || isSet || isMap


    val isNullableEnum: Boolean
        get() = this.fieldType is EnumFieldType && nullable


    val isEnumList: Boolean
        get() = fieldType is ListFieldType && this.fieldType.parameterFieldType is EnumFieldType


    val isEnum: Boolean = this.fieldType is EnumFieldType


    val minConstraint: MinConstraintDef?
        get() = validationConstraints.filterIsInstance<MinConstraintDef>().firstOrNull()


    val maxConstraint: MaxConstraintDef?
        get() = validationConstraints.filterIsInstance<MaxConstraintDef>().firstOrNull()


    val lengthConstraint: LengthConstraintDef?
        get() = validationConstraints.filterIsInstance<LengthConstraintDef>().firstOrNull()


    val getterMethodName: String
        get() = "get" + this.classFieldName.firstToUpper()


    val setterMethodName: String
        get() = "set" + this.classFieldName.firstToUpper()


    val isNumeric = this.fieldType.isNumeric()


    val isVersionField = this.classFieldName == ClassFieldName.version


    val defaultFormFieldValue = providedDefaultFormFieldValue ?: fieldType.defaultFormFieldValue


    init {

        val questionMark = if (this.nullable) "?" else ""
        this.unqualifiedToString = "${this.fieldType.unqualifiedToString}$questionMark"

    }


    private fun enrichWithImplicitValidationConstraints(
        providedValidationConstraints: SortedSet<AbstractValidationConstraintDef>
    ): SortedSet<AbstractValidationConstraintDef> {

        val enrichedConstraints = providedValidationConstraints.toMutableSet()

        if (this.fieldType is EnumFieldType) {
            enrichedConstraints.add(EnumConstraintDef(fieldType.fqcn))
        }

        if (this.nullable == false) {

            if (this.fieldType is DomainIdFieldType) {
                enrichedConstraints.add(NotNullConstraintDef.INSTANCE)
            } else if (this.fieldType.isStringBased()) {
                enrichedConstraints.add(NotBlankConstraintDef.INSTANCE)
            } else {
                enrichedConstraints.add(NotNullConstraintDef.INSTANCE)
            }

        }

        return enrichedConstraints.toSortedSet()

    }


    override fun toString(): String {

        return "ClassFieldDef{" +
            "classFieldName=" + classFieldName +
            ", fieldType=" + fieldType +
            ", modifiable=" + isModifiableBySystem +
            ", nullability=" + nullability +
            '}'.toString()

    }


    fun hasAnyValidationConstraint() : Boolean {

        return this.validationConstraints.isNotEmpty()

    }


    fun hasValidationConstraint(validationConstraintType: Class<out AbstractValidationConstraintDef>): Boolean {

        return this.validationConstraints.any { v -> validationConstraintType.isAssignableFrom(v.javaClass) }

    }


    val asIdAndNameDtoClassFieldDef: ClassFieldDef
        get() {

            if (fieldType is ForeignKeyFieldType) {

                return aClassField(
                    fieldType.foreignKeyFieldDef.foreignKeyFieldName.value,
                    FieldTypes.idAndName(fieldType.foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef)
                ).build()

            } else {
                throw RuntimeException("Unable to produce an IdAndNameDto from this field because it is not a foreign key reference.")
            }

        }


    fun unWrapIfComplexType(): ClassFieldDef {

        return copy(fieldType = this.fieldType.unwrap())

    }


    override fun compareTo(other: ClassFieldDef): Int {

        return this.classFieldName.compareTo(other.classFieldName)

    }


    fun convertToNullable(): ClassFieldDef {

        return if (this.nullable) {
            this
        } else {
            copy(nullability = Nullability.NULLABLE)
        }

    }


    fun convertToUnmodifiable(): ClassFieldDef {

        return if (this.isModifiableBySystem == false && this.isEditableByUser.value == false) {
            this
        } else {
            copy(isModifiableBySystem = false, isEditableByUser = IsEditableByUser.FALSE)
        }

    }


    fun convertToNotUnique(): ClassFieldDef {

        return if (this.isUnique == false) {
            this
        } else {
            copy(isUnique = false)
        }

    }


    fun hasAnyValidationConstraints(): Boolean {

        return this.validationConstraints.isNotEmpty()

    }


    fun withFieldName(fieldName: String): ClassFieldDef {
        return copy(classFieldName = ClassFieldName(fieldName))
    }


    fun withFieldType(fieldType: FieldType): ClassFieldDef {
        return copy(fieldType = fieldType)
    }


    fun findEmbeddedField(fieldPath: List<String>): ClassFieldDef {

        if (this.fieldType is EsDocFieldType) {
            return this.fieldType.esDocDef.findFieldByPath(fieldPath.joinToString("."))
        } else {
            throw IllegalStateException("This class field '$classFieldName' does not represent an embedded field.")
        }

    }


    fun copyWith(constraints: Set<AbstractValidationConstraintDef>): ClassFieldDef {

        return this.copy(providedValidationConstraints = this.providedValidationConstraints.plus(constraints).toSortedSet())

    }


    fun resultSetAdapterReadFunctionName(nullable: Boolean): String {

        val nullableSuffix = if (nullable) "OrNull" else ""

        return when (fieldType) {
            is BooleanFieldType -> "readBoolean$nullableSuffix"
            is BooleanTypeFieldType -> "readBoolean$nullableSuffix"
            is BooleanValueClassFieldType -> "readBoolean$nullableSuffix"
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> "readDomainId$nullableSuffix"
            is DoubleFieldType -> "readDouble$nullableSuffix"
            is EnumFieldType -> "readEnum$nullableSuffix"
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> "readDomainId$nullableSuffix"
            is FqcnFieldType -> TODO("YAGNI?")
            is IdAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> "readInstant$nullableSuffix"
            is IntFieldType -> "readInt$nullableSuffix"
            is IntTypeFieldType -> "readInt$nullableSuffix"
            is IntValueClassFieldType -> "readInt$nullableSuffix"
            is ListFieldType -> "TODO" //TODO("YAGNI?") //"read$nullableSuffix"
            is LocalDateFieldType -> "readLocalDate$nullableSuffix"
            is LongFieldType -> "readLong$nullableSuffix"
            is LongTypeFieldType -> "readLong$nullableSuffix"
            is MapFieldType -> "read${fieldType.uqcn}$nullableSuffix"
            is ObjectIdFieldType -> TODO("YAGNI?")
            is PeriodFieldType -> "readPeriod$nullableSuffix"
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> "readJson$nullableSuffix"
            is StringFieldType -> "readString$nullableSuffix"
            is StringTypeFieldType -> "readString$nullableSuffix"
            is StringValueClassFieldType -> "readString$nullableSuffix"
            is UrlFieldType -> "readUrl$nullableSuffix"
        }

    }


    class Builder internal constructor(
        private val classFieldName: ClassFieldName,
        private val fieldType: FieldType
    ) {

        private var description: Description? = null
        private var fieldDisplayName: FieldDisplayName? = null
        private var modifiable: Boolean = false
        private var nullability: Nullability = Nullability.NOT_NULLABLE
        private val annotationDefs = sortedSetOf<AnnotationDef>()
        private val validationConstraints = mutableSetOf<AbstractValidationConstraintDef>()
        private var isMasked: Boolean = false
        private var isPrivateProperty = false
        private var isConstructorOnly = false
        private var isUnique = false
        private var enrichWithImplicitValidationConstraints: Boolean = false
        private var textCase: TextCase = TextCase.ORIGINAL


        fun build(): ClassFieldDef {

            return ClassFieldDef(
                classFieldName = this.classFieldName,
                description = this.description,
                fieldType = this.fieldType,
                displayName = this.fieldDisplayName,
                isModifiableBySystem = this.modifiable,
                nullability = this.nullability,
                isMasked = this.isMasked,
                isPrivateProperty = this.isPrivateProperty,
                isConstructorOnly = this.isConstructorOnly,
                isUnique = this.isUnique,
                textCase = this.textCase,
                annotationDefs = this.annotationDefs,
                providedValidationConstraints = validationConstraints.toSortedSet()
            )

        }


        fun displayName(displayName: String): Builder {

            this.fieldDisplayName = FieldDisplayName(displayName)
            return this

        }


        fun modifiable(): Builder {

            this.modifiable = true
            return this

        }


        fun privat(): Builder {

            this.isPrivateProperty = true
            return this

        }


        fun unique(): Builder {

            this.isUnique = true
            return this

        }


        fun nullable(): Builder {

            this.nullability = Nullability.NULLABLE
            return this

        }


        fun nullability(nullability: Nullability): Builder {

            this.nullability = nullability
            return this

        }


        fun masked(): Builder {

            this.isMasked = true
            return this

        }


        fun description(description: String): Builder {

            this.description = Description(description)
            return this

        }


        fun enrichWithImplicitValidationConstraints(): Builder {

            this.enrichWithImplicitValidationConstraints = true
            return this

        }


        fun addAnnotation(annotationDef: AnnotationDef): Builder {

            this.annotationDefs.add(annotationDef)
            return this

        }


        fun addAnnotations(annotationDefs: Collection<AnnotationDef>): Builder {

            this.annotationDefs.addAll(annotationDefs)
            return this

        }


        fun withEmailConstraint(): Builder {

            this.validationConstraints.add(EmailConstraintDef.INSTANCE)
            return this

        }


        fun lengthConstraint(min: Long? = null, max: Long? = null) {

            this.validationConstraints.add(LengthConstraintDef.of(min, max))

        }


        fun forceToUpperCase(): Builder {

            this.textCase = TextCase.UPPER
            return this

        }


        fun forceToLowerCase(): Builder {

            this.textCase = TextCase.LOWER
            return this

        }


        fun constructorOnly(flag: Boolean = true): Builder {

            this.isConstructorOnly = flag
            return this

        }


    }


    companion object {


        fun aClassField(
            classFieldName: String,
            fqcn: Fqcn,
            init: (Builder.() -> Unit)? = null
        ): Builder {

            return aClassField(
                classFieldName,
                FieldTypes.byFqcn(fqcn),
                init
            )

        }


        fun aClassField(
            classFieldName: String,
            fieldType: FieldType,
            init: (Builder.() -> Unit)? = null
        ): Builder {

            val builder = Builder(
                ClassFieldName(classFieldName),
                fieldType
            )

            init?.invoke(builder)
            return builder

        }


        fun aClassField(
            classFieldName: ClassFieldName,
            fieldType: FieldType,
            init: (Builder.() -> Unit)? = null
        ): Builder {

            val builder = Builder(
                classFieldName,
                fieldType
            )

            init?.invoke(builder)
            return builder

        }


        fun aClassField(
            classFieldName: String,
            foreignKeyFieldDef: ForeignKeyFieldDef,
            init: (Builder.() -> Unit)? = null
        ): Builder {

            val builder = Builder(
                ClassFieldName(classFieldName),
                FieldTypes.foreignKey(foreignKeyFieldDef)
            )

            init?.invoke(builder)
            return builder

        }


    }


}
