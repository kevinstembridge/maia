package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityCreateDialogReactiveFormHtmlRenderer(
    private val createApiDef: EntityCreateApiDef
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
