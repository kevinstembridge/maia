package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.DatabaseIndexDef
import org.maiaframework.gen.spec.definition.IndexFieldDef

class AsyncValidatorRenderer(
    private val databaseIndexDef: DatabaseIndexDef,
    private val entityCrudApiDef: EntityCrudApiDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.databaseIndexDef.asyncValidator.asyncValidatorRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |import { AbstractControl, AsyncValidator, ValidationErrors, FormControl, FormGroup } from '@angular/forms';
            |import { Injectable } from '@angular/core';
            |import { Observable, of } from 'rxjs';
            |import { catchError, map } from 'rxjs/operators';
            |${this.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceImportStatement}
            |${this.databaseIndexDef.asyncValidator.asyncValidationDtoImportStatement}
            |
            |
            |@Injectable({
            |    providedIn: 'root'
            |})
            |export class ${this.databaseIndexDef.asyncValidator.asyncValidatorName} implements AsyncValidator {
            |
            |
            |    constructor(
            |        private apiService: ${this.databaseIndexDef.apiServiceName}
            |    ) { }
        """.trimMargin())

        if (this.databaseIndexDef.isMultiField) {
            renderValidateFunctionForMultiFieldIndex()
        } else {
            renderValidateFunctionForSingleFieldIndex()
        }

        append("""
            |
            |
            |}
            |""".trimMargin())

    }


    private fun renderValidateFunctionForMultiFieldIndex() {

        append("""
            |
            |
            |
            |    validate(ctrl: FormGroup): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
            |
            |        if (ctrl.dirty === false) {
            |            return of(null);
            |        }
            |
            |""".trimMargin())

        val controlNames = mutableListOf<String>()

        this.databaseIndexDef.indexDef.indexFieldDefs.forEach { indexFieldDef ->

            val typeaheadDef = indexFieldDef.entityFieldDef.typeaheadDef

            if (typeaheadDef != null) {
                val controlName = "${typeaheadDef.typeaheadName.firstToLower()}Control"
                controlNames.add(controlName)
                appendLine("        const $controlName = ctrl.get('${typeaheadDef.typeaheadName.firstToLower()}');")
            } else {
                val controlName = "${indexFieldDef.entityFieldDef.classFieldName}Control"
                controlNames.add(controlName)
                appendLine("        const $controlName = ctrl.get('${indexFieldDef.entityFieldDef.classFieldName}');")
            }

        }

        append("""
            |
            |        if (${controlNames.joinToString(" && ")} && ${controlNames.map { "$it.value" }.joinToString(" && ")}) {
            |
            |            const requestBody = {
            |""".trimMargin())

        this.databaseIndexDef.indexDef.indexFieldDefs
            .sortedWith(compareBy<IndexFieldDef> { it.entityFieldDef.classFieldDef.nullable }.thenBy { it.entityFieldDef.classFieldName })
            .forEach { indexFieldDef ->

                val typeaheadDef = indexFieldDef.entityFieldDef.typeaheadDef

                val entityFieldName = indexFieldDef.entityFieldDef.classFieldName

                if (typeaheadDef != null) {
                    // TODO The name of the form control field may be different that the we have to find the field name of the EsDoc field, not the field name of the entity field.
                    appendLine("                $entityFieldName: ${typeaheadDef.typeaheadName.firstToLower()}Control.value.$entityFieldName,")
                } else {
                    appendLine("                $entityFieldName: ${entityFieldName.firstToLower()}Control.value,")
                }

            }

        append("""
            |            } as ${this.databaseIndexDef.asyncValidator.asyncValidationDtoName};
            |
            |            return this.apiService.${this.databaseIndexDef.existsByFunctionName}(requestBody).pipe(
            |                map(response => (response.invalid ? { message: response.message, notUnique: true } : null)),
            |                catchError(() => of(null))
            |            );
            |
            |        } else {
            |            return of(null);
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun renderValidateFunctionForSingleFieldIndex() {

        append("""
            |
            |
            |
            |    validate(ctrl: FormControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
            |
            |        if (ctrl.value === false || ctrl.dirty === false) {
            |            return of(null);
            |        }
            |
            |        const requestBody = {
            |""".trimMargin())

        this.databaseIndexDef.indexDef.indexFieldDefs.forEach { indexFieldDef ->

            val typeaheadDef = indexFieldDef.entityFieldDef.typeaheadDef

            if (typeaheadDef != null) {
                appendLine("            ${indexFieldDef.entityFieldDef.classFieldName}: ${typeaheadDef.typeaheadName.firstToLower()}.value.${indexFieldDef.entityFieldDef.classFieldName}")
            } else {
                appendLine("            ${indexFieldDef.entityFieldDef.classFieldName}: ctrl.value")
            }

        }

        append("""
            |        } as ${this.databaseIndexDef.asyncValidator.asyncValidationDtoName};
            |
            |        return this.apiService.${this.databaseIndexDef.existsByFunctionName}(requestBody).pipe(
            |            map(response => (response.invalid ? { message: response.message, notUnique: true } : null)),
            |            catchError(() => of(null))
            |        );
            |
            |    }
            |""".trimMargin())

    }


}
