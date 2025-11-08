package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException.Companion.throwIfBlank
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.FieldLabel
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.HtmlFormDef
import org.maiaframework.gen.spec.definition.HtmlFormFieldDef
import org.maiaframework.gen.spec.definition.HtmlInputType
import org.maiaframework.gen.spec.definition.RequestDtoDef
import java.util.ArrayList


@MaiaDslMarker
class RequestDtoHtmlFormDefBuilder(private val requestDtoDef: RequestDtoDef) {


    private val fieldBuilders = ArrayList<FieldBuilder>()
    private var onSuccessUrl: String? = null
    private val withPreAuthorize: WithPreAuthorize? = null


    fun field(fieldName: String): FieldBuilder {

        val fieldBuilder = FieldBuilder(fieldName, this)
        this.fieldBuilders.add(fieldBuilder)

        return fieldBuilder

    }


    fun onSuccessUrl(onSuccessUrl: String): RequestDtoHtmlFormDefBuilder {

        this.onSuccessUrl = if (onSuccessUrl.isNotBlank()) onSuccessUrl else null
        return this

    }


    fun build(): HtmlFormDef {

        val fieldDefs = this.fieldBuilders.map { this.buildField(it) }

        return HtmlFormDef(
                this.requestDtoDef,
                fieldDefs,
                this.onSuccessUrl,
                this.withPreAuthorize)

    }


    private fun buildField(fieldBuilder: FieldBuilder): HtmlFormFieldDef {

        val fieldDef = this.requestDtoDef.findFieldByName(fieldBuilder.fieldName)

        return HtmlFormFieldDef(
                this.requestDtoDef.dtoBaseName,
                fieldDef.classFieldDef,
                fieldBuilder.fieldLabel,
                fieldBuilder.placeholder,
                fieldBuilder.htmlInputType)

    }


    class FieldBuilder(fieldName: String, private val enclosingBuilder: RequestDtoHtmlFormDefBuilder) {

        val fieldName: String = throwIfBlank(fieldName, "fieldName")
        var fieldLabel: FieldLabel? = null
        var placeholder: FormPlaceholderText? = null
        var htmlInputType = HtmlInputType.text


        fun withLabel(fieldLabel: String): FieldBuilder {

            this.fieldLabel = if (fieldLabel.isNotBlank()) FieldLabel(fieldLabel) else null
            return this

        }


        fun withPlaceholder(placeholder: String): FieldBuilder {

            this.placeholder = if (placeholder.isNotBlank()) FormPlaceholderText(placeholder) else null
            return this

        }


        fun and(): RequestDtoHtmlFormDefBuilder {

            return this.enclosingBuilder

        }


        fun withHtmlInputType(htmlInputType: HtmlInputType): FieldBuilder {

            this.htmlInputType = htmlInputType
            return this

        }


    }


}
