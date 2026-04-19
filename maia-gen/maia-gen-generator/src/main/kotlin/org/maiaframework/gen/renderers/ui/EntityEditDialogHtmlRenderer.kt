package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityEditDialogHtmlRenderer(
    private val updateApiDef: EntityUpdateApiDef,
    private val chipFields: List<ManyToManyChipFieldDef> = emptyList()
) : AbstractCrudFormHtmlRenderer(
    updateApiDef.entityDef,
    InlineFormOrDialog.DIALOG
) {


    override val dialogTitle = "Edit"


    override val formFields: List<AngularFormFieldDef> = updateApiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return this.updateApiDef.angularDialogComponentNames.htmlRenderedFilePath

    }

    // TODO this is duplicated in the Create renderer
    override fun renderManyToManyChipFields() {

        chipFields.forEach { chip ->
            appendLine("""
                |        <mat-form-field appearance="outline">
                |            <mat-label>${chip.labelText}</mat-label>
                |            <mat-chip-grid #chipGrid>
                |                @for (entity of ${chip.selectedFieldName}; track entity.${chip.esDocIdFieldName}) {
                |                    <mat-chip-row (removed)="${chip.removeMethodName}(entity)">
                |                        {{ entity.${chip.searchTermFieldName} }}
                |                        <button matChipRemove type="button"><mat-icon>cancel</mat-icon></button>
                |                    </mat-chip-row>
                |                }
                |            </mat-chip-grid>
                |            <input
                |                #${chip.inputRefName}
                |                placeholder="${chip.searchPlaceholder}"
                |                [formControl]="${chip.searchControlFieldName}"
                |                [matChipInputFor]="chipGrid"
                |                [matAutocomplete]="${chip.autocompleteRefName}"
                |            />
                |            <mat-autocomplete #${chip.autocompleteRefName}="matAutocomplete" (optionSelected)="${chip.addMethodName}(${'$'}event)">
                |                @if (${chip.filteredIsLoadingFieldName}()) {
                |                    <mat-option disabled>Loading...</mat-option>
                |                }
                |                @for (option of ${chip.filteredFieldName}; track option.${chip.esDocIdFieldName}) {
                |                    <mat-option [value]="option">{{ option.${chip.searchTermFieldName} }}</mat-option>
                |                }
                |            </mat-autocomplete>
                |        </mat-form-field>""".trimMargin())
        }

    }


}
