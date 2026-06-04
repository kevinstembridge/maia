package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityCreateReactiveFormHtmlRenderer(
    private val entityCreatePageDef: EntityCreatePageDef,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList(),
    override val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractCrudReactiveFormHtmlRenderer(
    entityCreatePageDef.entityDef,
    InlineFormOrDialog.INLINE_FORM
) {


    override val dialogTitle: String = "ignored"


    override val withCancelButton = true


    override val formFields: List<AngularFormFieldDef> = entityCreatePageDef.createApiDef.htmlFormFields


    override fun renderedFilePath(): String {

        return entityCreatePageDef.createFormAngularComponentNames.htmlRenderedFilePath

    }


}
