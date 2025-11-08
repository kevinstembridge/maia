package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityEditDialogHtmlRenderer(
    private val apiDef: EntityUpdateApiDef
) : AbstractCrudFormHtmlRenderer(
    apiDef.entityDef,
    InlineFormOrDialog.DIALOG
) {


    override val dialogTitle = "Edit"


    override val formFields: List<AngularFormFieldDef> = apiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return this.apiDef.angularDialogComponentNames.htmlRenderedFilePath

    }


}
