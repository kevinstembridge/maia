package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

// TODO Delete after cooling off: 2027-01-18. Part of reactive forms, replaced by signal forms.
class EntityCreateDialogReactiveFormHtmlRenderer(
    private val createApiDef: EntityCreateApiDef,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList()
) : AbstractCrudReactiveFormHtmlRenderer(
    createApiDef.entityDef,
    InlineFormOrDialog.DIALOG
) {


    override val dialogTitle: String = "Create"


    override val formFields: List<AngularFormFieldDef> = createApiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return this.createApiDef.angularDialogComponentHtmlFilePath

    }


}
