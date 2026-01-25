package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.SimpleTypeDef
import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef


@MaiaDslMarker
class FormModelFieldDefBuilder private constructor(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val simpleTypeDef: SimpleTypeDef?,
    private val formModelDefBuilder: FormModelDefBuilder
) {


    private var description: Description? = null
    private var optional = false
    private var nullability = Nullability.NOT_NULLABLE
    private var isMasked = false
    private var textCase = TextCase.ORIGINAL
    private val annotationDefs = sortedSetOf<AnnotationDef>()
    private val validationConstraints = mutableSetOf<AbstractValidationConstraintDef>()


    constructor(
        classFieldName: ClassFieldName,
        enumDef: EnumDef,
        formModelDefBuilder: FormModelDefBuilder
    ) : this(
        classFieldName,
        FieldTypes.enum(enumDef),
        null,
        formModelDefBuilder
    )


//    constructor(
//        classFieldName: ClassFieldName,
//        simpleTypeDef: SimpleTypeDef,
//        formModelDefBuilder: FormModelDefBuilder
//    ) : this(
//        classFieldName,
//        simpleTypeDef.fieldType,
//        simpleTypeDef,
//        formModelDefBuilder
//    )


    constructor(
        classFieldName: ClassFieldName,
        fieldType: FieldType,
        formModelDefBuilder: FormModelDefBuilder
    ) : this(
        classFieldName,
        fieldType,
        null,
        formModelDefBuilder
    )


    fun optional(): FormModelFieldDefBuilder {

        this.optional = true
        return this

    }


    fun nullable(): FormModelFieldDefBuilder {

        this.nullability = Nullability.NULLABLE
        return this

    }


    fun description(description: String): FormModelFieldDefBuilder {

        this.description = Description(description)
        return this

    }


    fun withEmailConstraint(): FormModelFieldDefBuilder {

        addValidationConstraint(EmailConstraintDef.INSTANCE)
        return this

    }


    fun build(): ClassFieldDef {

        return ClassFieldDef(
            this.classFieldName,
            this.description,
            this.fieldType,
            isModifiableBySystem = false,
            nullability = this.nullability,
            isMasked = this.isMasked,
            isPrivateProperty = false,
            isUnique = false,
            typeaheadDef = null,
            displayName = null,
            formPlaceholderText = null,
            textCase = this.textCase,
            annotationDefs = this.annotationDefs.toSortedSet(),
            providedValidationConstraints = this.validationConstraints.toSortedSet()
        )

    }


    private fun addValidationConstraint(validationConstraint: AbstractValidationConstraintDef) {

        this.validationConstraints.add(validationConstraint)
        this.annotationDefs.add(validationConstraint.associatedAnnotationDef)

    }


    fun masked(): FormModelFieldDefBuilder {

        this.isMasked = true
        return this

    }


    fun and(): FormModelDefBuilder {

        return this.formModelDefBuilder

    }


}
