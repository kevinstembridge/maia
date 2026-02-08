package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityCreateReactiveFormHtmlRenderer(
    private val createApiDef: EntityCreateApiDef
) : AbstractCrudReactiveFormHtmlRenderer(
    createApiDef.entityDef,
    InlineFormOrDialog.INLINE_FORM
) {


    override val dialogTitle: String = "ignored"


    override val formFields: List<AngularFormFieldDef> = createApiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return this.createApiDef.angularEntityFormComponentHtmlFilePath

    }


}
