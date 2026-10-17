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
            append("""
                |        <${chip.typeaheadDef.angularComponentSelector}
                |            chips
                |            label="${chip.labelText}"
                |            placeholder="${chip.searchPlaceholder}"
                |            [selected]="${chip.selectedFieldName}"
                |            (added)="${chip.addMethodName}(${'$'}event)"
                |            (removed)="${chip.removeMethodName}(${'$'}event)"
                |        ></${chip.typeaheadDef.angularComponentSelector}>
                |""".trimMargin())
        }

    }


}
