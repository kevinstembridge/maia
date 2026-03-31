package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityFieldDef

class TypeaheadFieldValidatorRenderer(
    private val entityFieldDef: EntityFieldDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/forms", "AbstractControl")
        addImport("@angular/forms", "ValidatorFn")

    }


    override fun renderedFilePath(): String {

        return this.entityFieldDef.typeaheadRequiredValidatorFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |export function ${this.entityFieldDef.typeaheadRequiredValidatorFunctionName}(): ValidatorFn {
            |
            |    return (control: AbstractControl): {[key: string]: any} | null => {
            |
            |        if (control.value && control.value.${entityFieldDef.typeaheadDef!!.esDocIdFieldName}) {
            |            return null;
            |        } else {
            |            return { required: true };
            |        }
            |
            |    };
            |
            |}
            |""".trimMargin())

    }


}
