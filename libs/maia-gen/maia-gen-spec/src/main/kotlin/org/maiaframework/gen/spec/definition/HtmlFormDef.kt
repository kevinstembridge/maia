package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import java.util.*


class HtmlFormDef(
    requestDtoDef: RequestDtoDef,
    val allHtmlFormFields: List<HtmlFormFieldDef>,
    val onSuccessUrl: String?,
    val withPreAuthorize: WithPreAuthorize?) {


    private val htmlFormKey: HtmlFormKey
    val htmlFormName: HtmlFormName
    val formSubmissionPath: String

    init {

        val dtoKey = requestDtoDef.dtoBaseName

        this.htmlFormKey = HtmlFormKey(dtoKey.toString() + "Form")
        this.htmlFormName = HtmlFormName(this.htmlFormKey.value)
        this.formSubmissionPath = "/api/$dtoKey"

    }


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val entityDef = other as HtmlFormDef?
        return htmlFormKey == entityDef!!.htmlFormKey

    }


    override fun hashCode(): Int {

        return Objects.hash(htmlFormKey)

    }


    override fun toString(): String {

        return "HtmlFormDef{" + this.htmlFormKey + "}"

    }


}
