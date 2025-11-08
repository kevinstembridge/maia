package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityEditFormHtmlRenderer(
    private val apiDef: EntityUpdateApiDef
) : AbstractCrudFormHtmlRenderer(
    apiDef.entityDef,
    InlineFormOrDialog.INLINE_FORM
) {


    override val dialogTitle = "ignored"


    override val formFields: List<AngularFormFieldDef> = apiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return this.apiDef.angularFormComponentNames.htmlRenderedFilePath

    }


}
