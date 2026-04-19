package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityCreateDialogHtmlRenderer(
    private val createApiDef: EntityCreateApiDef,
    private val chipFields: List<ManyToManyChipFieldDef> = emptyList()
) : AbstractCrudFormHtmlRenderer(
    createApiDef.entityDef,
    InlineFormOrDialog.DIALOG
) {


    override val dialogTitle: String = "Create"


    override val formFields: List<AngularFormFieldDef> = createApiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return this.createApiDef.angularDialogComponentHtmlFilePath

    }


    // TODO MTM: this is duplicated in the Edit renderer
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
