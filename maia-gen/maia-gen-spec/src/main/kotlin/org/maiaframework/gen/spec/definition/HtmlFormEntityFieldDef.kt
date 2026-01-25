package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.EnumConstraintDef
import org.maiaframework.gen.spec.definition.validation.MaxConstraintDef
import org.maiaframework.gen.spec.definition.validation.MinConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotEmptyConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef


class HtmlFormEntityFieldDef(
    val entityFieldDef: EntityFieldDef,
    val label: FieldLabel?,
    val placeholder: FormPlaceholderText?,
    val htmlInputType: HtmlInputType
) {


    val classFieldDef: ClassFieldDef
        get() = this.entityFieldDef.classFieldDef


    val fieldName: ClassFieldName
        get() = classFieldDef.classFieldName


    val fieldKey: EntityFieldKey
        get() = this.entityFieldDef.key


//    val entityBaseName: EntityBaseName
//        get() = this.entityFieldDef.entityBaseName


    val isRequired: Boolean
        get() = hasValidationConstraint(NotNullConstraintDef::class.java) || hasValidationConstraint(NotEmptyConstraintDef::class.java)


    val isNumeric: Boolean
        get() = this.classFieldDef.isNumeric


    val minConstraint: MinConstraintDef?
        get() = this.classFieldDef.minConstraint


    val maxConstraint: MaxConstraintDef?
        get() = this.classFieldDef.maxConstraint

    val isEditable: Boolean
        get() = this.classFieldDef.isModifiableBySystem


    fun hasValidationConstraint(type: Class<out AbstractValidationConstraintDef>): Boolean {

        if (this.classFieldDef.hasValidationConstraint(type)) {
            return true
        }

        if (NotEmptyConstraintDef::class.java.isAssignableFrom(type) || NotNullConstraintDef::class.java.isAssignableFrom(type)) {
            return this.classFieldDef.nullable == false
        }

        return if (EnumConstraintDef::class.java.isAssignableFrom(type)) {
            this.classFieldDef.fieldType is EnumFieldType
        } else
            false

    }


}
