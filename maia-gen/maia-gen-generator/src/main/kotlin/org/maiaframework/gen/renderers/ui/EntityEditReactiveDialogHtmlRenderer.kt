package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog

// TODO Delete after cooling off: 2027-01-18. Part of reactive forms, replaced by signal forms.
class EntityEditReactiveDialogHtmlRenderer(
    private val apiDef: EntityUpdateApiDef,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList()
) : AbstractCrudReactiveFormHtmlRenderer(
    apiDef.entityDef,
    InlineFormOrDialog.DIALOG
) {


    override val dialogTitle = "Edit"


    override val formFields: List<AngularFormFieldDef> = apiDef.htmlFormFields


    override val withFetchForEditLoading: Boolean = apiDef.entityDef.fetchForEditDtoDef != null


    override fun renderedFilePath(): String {

        return this.apiDef.angularDialogComponentNames.htmlRenderedFilePath

    }


}
