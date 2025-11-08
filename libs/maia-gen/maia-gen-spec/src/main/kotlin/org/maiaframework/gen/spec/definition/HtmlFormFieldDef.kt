package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotEmptyConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef


class HtmlFormFieldDef(
        private val dtoBaseName: DtoBaseName,
        val classFieldDef: ClassFieldDef,
        val label: FieldLabel?,
        val placeholder: FormPlaceholderText?,
        val htmlInputType: HtmlInputType
) {


    val fieldName: ClassFieldName = classFieldDef.classFieldName


    val isRequired: Boolean
        get() = hasValidationConstraint(NotNullConstraintDef::class.java) || hasValidationConstraint(
                NotEmptyConstraintDef::class.java
        )


    val isNumeric: Boolean = this.classFieldDef.isNumeric


    val isEditable: Boolean = this.classFieldDef.isModifiableBySystem


    val fieldKey: String = this.dtoBaseName.toString() + "_" + fieldName


    fun hasValidationConstraint(type: Class<out AbstractValidationConstraintDef>): Boolean {

        return this.classFieldDef.hasValidationConstraint(type)

    }


}
