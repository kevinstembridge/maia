package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityFieldDef

class TypeaheadFieldValidatorRenderer(
    private val entityFieldDef: EntityFieldDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.entityFieldDef.typeaheadRequiredValidatorFilePath

    }


    override fun renderSourceBody() {

        append("""
            |import { AbstractControl, ValidatorFn } from '@angular/forms';
            |
            |export function ${this.entityFieldDef.typeaheadRequiredValidatorFunctionName}(): ValidatorFn {
            |
            |  return (control: AbstractControl): {[key: string]: any} | null => {
            |
            |    if (control.value && control.value.${entityFieldDef.typeaheadDef!!.idFieldName}) {
            |      return null;
            |    } else {
            |      return { required: true };
            |    }
            |
            |  };
            |
            |}
            |""".trimMargin())

    }


}
