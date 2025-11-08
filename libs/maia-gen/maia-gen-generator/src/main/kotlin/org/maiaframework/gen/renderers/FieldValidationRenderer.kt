package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName

class FieldValidationRenderer(private val fieldDef: ClassFieldDef) : AbstractSourceRenderer() {

    private val fieldName: ClassFieldName = fieldDef.classFieldName


    public override fun renderSource(): String {

        if (fieldDef.nullable == false) {

            blankLine()
            appendLine("    if (!fieldValues.$fieldName) {")
            appendLine("        errors.$fieldName = 'This is a required field.'")
            appendLine("    }")

        }

        if (fieldDef.isNumeric) {

            blankLine()
            appendLine("    if (fieldValues.${fieldDef.classFieldName} && _.isFinite(Number(fieldValues.${fieldDef.classFieldName})) === false) {")
            appendLine("        errors.${fieldDef.classFieldName} = 'This field must be a number.'")
            appendLine("    }")

        }

        // TODO handle other constraint types

        if (fieldDef.isNumeric && fieldDef.minConstraint != null) {

            val minValue = fieldDef.minConstraint!!.minValue

            appendLine("    else if (fieldValues.$fieldName && Number(fieldValues.$fieldName) < $minValue) {")
            appendLine("        errors.$fieldName = 'This field cannot be less than $minValue.'")
            appendLine("    }")

        }

        if (fieldDef.isNumeric && fieldDef.maxConstraint != null) {

            val maxValue = fieldDef.maxConstraint!!.maxValue

            appendLine("    else if (fieldValues.$fieldName && Number(fieldValues.$fieldName) > $maxValue) {")
            appendLine("        errors.$fieldName = 'This field cannot be greater than $maxValue.'")
            appendLine("    }")

        }

        return sourceCode.toString()

    }


}
