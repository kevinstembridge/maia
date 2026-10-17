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
