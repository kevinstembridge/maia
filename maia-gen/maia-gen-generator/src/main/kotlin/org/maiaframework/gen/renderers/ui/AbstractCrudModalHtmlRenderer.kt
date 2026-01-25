package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef

abstract class AbstractCrudModalHtmlRenderer(
    protected val entityCrudApiDef: EntityCrudApiDef,
    private val modalComponentHtmlFileName: String
) : AbstractSourceFileRenderer() {


    protected abstract val modalTitle: String

    protected abstract val formFields: List<ClassFieldDef>


    override fun renderedFilePath(): String {

        return "app/gen-components/${this.entityCrudApiDef.entityDef.entityBaseName.toKebabCase()}/${modalComponentHtmlFileName}"

    }


    override fun renderSource(): String {

        appendLine("<div class=\"modal-header\">")
        appendLine("  <h4 class=\"modal-title\" id=\"modal-basic-title\">${this.modalTitle}</h4>")
        appendLine("  <button type=\"button\"  class=\"close\" aria-label=\"Close\" (click)=\"activeModal.dismiss('Cross click')\">")
        appendLine("    <span aria-hidden=\"true\">&times;</span>")
        appendLine("  </button>")
        appendLine("</div>")
        blankLine()
        appendLine("<form [formGroup]=\"formGroup\" (ngSubmit)=\"submitForm()\">")
        appendLine("  <div class=\"modal-body\">")
        appendLine("    <div class=\"container\">")

        if (this.entityCrudApiDef.entityDef.multiFieldUniqueIndexDefs.isNotEmpty()) {
            appendLine("      <div *ngIf=\"formGroup.errors?.message && (formGroup.touched || formGroup.dirty)\" class=\"form-level-error-message alert alert-danger\">")
            appendLine("        {{formGroup.errors.message}}")
            appendLine("      </div>")
        }

        formFields.forEach { classFieldDef ->

            val classFieldName = classFieldDef.classFieldName
            val typeaheadDef = classFieldDef.typeaheadDef

            if (typeaheadDef == null) {

                appendLine("      <div class=\"form-group\">")
                if (classFieldDef.displayName != null) {
                    appendLine("        <label for=\"$classFieldName\">${classFieldDef.displayName}</label>")
                }
                appendLine("        <input id=\"$classFieldName\" type=\"text\" class=\"form-control\" formControlName=\"$classFieldName\" [ngClass]=\"{ 'is-invalid': submitted && f.${classFieldName}.errors }\">")

                if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {

                    appendLine("        <div *ngIf=\"submitted && f.$classFieldName.errors\" class=\"form-control-invalid\">")

                    if (classFieldDef.hasValidationConstraint(NotNullConstraintDef::class.java) || classFieldDef.hasValidationConstraint(NotBlankConstraintDef::class.java)) {
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.required\">${classFieldDef.displayName} is required.</div>")
                    }

                    if (classFieldDef.hasValidationConstraint(EmailConstraintDef::class.java)) {
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.email\">${classFieldDef.displayName} must be a valid email address.</div>")
                    }

                    classFieldDef.minConstraint?.minValue?.let { min ->
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.min\">${classFieldDef.displayName} must be at least $min.</div>")
                    }

                    classFieldDef.maxConstraint?.maxValue?.let { max ->
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.max\">${classFieldDef.displayName} must not be more than $max.</div>")
                    }

                    classFieldDef.lengthConstraint?.min?.let { minLength ->
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.minlength\">${classFieldDef.displayName} must be at least $minLength characters.</div>")
                    }

                    classFieldDef.lengthConstraint?.max?.let { maxLength ->
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.maxlength\">${classFieldDef.displayName} must not be more than $maxLength characters.</div>")
                    }

                    if (classFieldDef.isUnique) {
                        appendLine("          <div *ngIf=\"f.$classFieldName.errors.message\">{{f.$classFieldName.errors.message}}</div>")
                    }

                    appendLine("        </div>")

                }

                appendLine("      </div>")

            } else {

                val formGroupFieldName = typeaheadDef.typeaheadName.firstToLower()

                appendLine("      <div class=\"form-group\">")
                appendLine("        <label for=\"${formGroupFieldName}\">${classFieldDef.displayName}</label>")
                appendLine("        <input")
                appendLine("          formControlName=\"$formGroupFieldName\"")
                appendLine("          id=\"${formGroupFieldName}\"")
                appendLine("          type=\"text\"")
                appendLine("          class=\"form-control\"")
                appendLine("          [ngbTypeahead]=\"${formGroupFieldName}Search\"")
                appendLine("          placeholder=\"${classFieldDef.formPlaceholderText}\"")
                appendLine("          [resultFormatter]=\"${formGroupFieldName}ResultFormatter\"")
                appendLine("          [inputFormatter]=\"${formGroupFieldName}InputFormatter\"")
                appendLine("          [editable]=\"false\"")
                appendLine("          [showHint]=\"true\"")
                appendLine("          [ngClass]=\"{ 'is-invalid': submitted && f.${formGroupFieldName}.errors }\" />")

                if (classFieldDef.nullable == false) {

                    appendLine("        <div *ngIf=\"submitted && f.$formGroupFieldName.errors\" class=\"form-control-invalid\">")
                    appendLine("          <div *ngIf=\"f.$formGroupFieldName.errors.required\">${classFieldDef.displayName} is required.</div>")
                    appendLine("        </div>")

                }

                appendLine("      </div>")

            }

        }

        appendLine("    </div>")
        appendLine("  </div>")
        appendLine("  <div class=\"modal-footer\">")
        appendLine("    <button class=\"btn btn-secondary\">Submit</button>")
        appendLine("    <button class=\"btn btn-secondary\" (click)=\"activeModal.dismiss()\">Cancel</button>")
        appendLine("  </div>")
        appendLine("</form>")

        return sourceCode.toString()

    }


}
