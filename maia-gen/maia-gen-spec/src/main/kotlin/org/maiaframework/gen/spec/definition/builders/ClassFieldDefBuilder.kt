package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import kotlin.reflect.KClass

class ClassFieldDefBuilder(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType
) {


    var modifiableBySystem = false


    var isEditableByUser = IsEditableByUser.FALSE


    var nullability = Nullability.NOT_NULLABLE


    var unique = false


    var isMasked = false


    private val annotationDefs = sortedSetOf<AnnotationDef>()


    private val validationConstraints = mutableSetOf<AbstractValidationConstraintDef>()


    var description: Description? = null


    var fieldDisplayName: FieldDisplayName? = null


    var formPlaceholderText: FormPlaceholderText? = null


    var textCase: TextCase = TextCase.ORIGINAL


    var typeaheadDef: TypeaheadDef? = null


    var valueMappings: Map<String, String>? = null


    var isPrivateProperty = false


    var isConstructorOnly = false


    fun addValidationConstraint(validationConstraint: AbstractValidationConstraintDef) {

        this.validationConstraints.add(validationConstraint)

    }


    fun addAnnotationDef(annotationDef: AnnotationDef) {

        this.annotationDefs.add(annotationDef)

    }


    fun build(): ClassFieldDef {

        return ClassFieldDef(
            annotationDefs = annotationDefs,
            classFieldName = classFieldName,
            description = description,
            displayName = fieldDisplayName,
            fieldType = fieldType,
            formPlaceholderText = formPlaceholderText,
            isConstructorOnly = isConstructorOnly,
            isEditableByUser = isEditableByUser,
            isMasked = isMasked,
            isModifiableBySystem = modifiableBySystem,
            isPrivateProperty = isPrivateProperty,
            isUnique = unique,
            nullability = nullability,
            providedValidationConstraints = validationConstraints.toSortedSet(),
            textCase = textCase,
            typeaheadDef = typeaheadDef,
            valueMappings = valueMappings
        )

    }


    fun removeValidationConstraint(constraintDefKClass: KClass<out AbstractValidationConstraintDef>) {

        this.validationConstraints.removeIf { it.javaClass == constraintDefKClass.java  }

    }


}
