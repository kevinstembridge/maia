package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import java.util.*


class EntityHtmlFormDef(
    entityDef: EntityDef,
    val allHtmlFormFields: List<HtmlFormEntityFieldDef>,
    val onSuccessUrl: String?,
    val withPreAuthorize: WithPreAuthorize?
) {


    private val htmlFormKey: HtmlFormKey
    val htmlFormName: HtmlFormName
    val formSubmissionPath: String

    init {

        val entityKey = entityDef.entityBaseName

        this.htmlFormKey = HtmlFormKey("Create" + entityKey + "Form")
        this.htmlFormName = HtmlFormName(this.htmlFormKey.value)
        this.formSubmissionPath = "/api/$entityKey"

    }


    override fun equals(o: Any?): Boolean {

        if (this === o) {
            return true
        }

        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val entityDef = o as EntityHtmlFormDef?
        return htmlFormKey == entityDef!!.htmlFormKey

    }


    override fun hashCode(): Int {

        return Objects.hash(htmlFormKey)

    }


    override fun toString(): String {

        return "EntityHtmlFormDef{" + this.htmlFormKey + "}"

    }


}
