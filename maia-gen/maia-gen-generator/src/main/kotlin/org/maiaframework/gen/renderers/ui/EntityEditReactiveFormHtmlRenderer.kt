package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

class EntityEditReactiveFormHtmlRenderer(
    private val apiDef: EntityUpdateApiDef,
    private val componentNamesOverride: AngularComponentNames? = null,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList(),
    override val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractCrudReactiveFormHtmlRenderer(
    apiDef.entityDef,
    InlineFormOrDialog.INLINE_FORM
) {


    override val dialogTitle = "ignored"


    override val withCancelButton = true


    override val formFields: List<AngularFormFieldDef> = apiDef.editHtmlFormFields


    override fun renderSingleFormField(formFieldDef: AngularFormFieldDef) {
        if (formFieldDef.isEditable) {
            MatFormFieldRenderer.renderFormField(formFieldDef, this)
        } else {
            MatFormFieldRenderer.renderReadOnlyField(formFieldDef, this)
        }
    }


    override fun renderedFilePath(): String {

        return (componentNamesOverride ?: this.apiDef.angularFormComponentNames).htmlRenderedFilePath

    }


}
