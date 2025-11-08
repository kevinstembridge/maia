package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException.Companion.throwIfBlank
import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityHtmlFormDef
import org.maiaframework.gen.spec.definition.FieldLabel
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.HtmlFormEntityFieldDef
import org.maiaframework.gen.spec.definition.HtmlInputType

class EntityCreateHtmlFormDefBuilder(private val entityDef: EntityDef) {


    private val fieldBuilders = mutableListOf<FieldBuilder>()
    private var onSuccessUrl: String? = null
    private val withPreAuthorize: WithPreAuthorize? = null


    fun field(fieldName: String): FieldBuilder {

        val fieldBuilder = FieldBuilder(fieldName, this)
        this.fieldBuilders.add(fieldBuilder)

        return fieldBuilder

    }


    fun onSuccessUrl(onSuccessUrl: String): EntityCreateHtmlFormDefBuilder {

        this.onSuccessUrl = StringFunctions.stripToNull(onSuccessUrl)
        return this

    }


    fun build(): EntityHtmlFormDef {

        val fieldDefs = this.fieldBuilders
                .map { this.buildField(it) }

        return EntityHtmlFormDef(
                this.entityDef,
                fieldDefs,
                this.onSuccessUrl,
                this.withPreAuthorize)

    }


    private fun buildField(fieldBuilder: FieldBuilder): HtmlFormEntityFieldDef {

        val entityFieldDef = this.entityDef.findFieldByName(fieldBuilder.fieldName)

        return HtmlFormEntityFieldDef(entityFieldDef, fieldBuilder.fieldLabel, fieldBuilder.placeholder, fieldBuilder.htmlInputType)

    }


    class FieldBuilder(fieldName: String, private val enclosingBuilder: EntityCreateHtmlFormDefBuilder) {

        val fieldName: String = throwIfBlank(fieldName, "fieldName")
        var fieldLabel: FieldLabel? = null
        var placeholder: FormPlaceholderText? = null
        var htmlInputType = HtmlInputType.text


        fun withLabel(fieldLabel: String): FieldBuilder {

            this.fieldLabel = StringFunctions.stripToNull(fieldLabel) { FieldLabel(it) }
            return this

        }


        fun withPlaceholder(placeholder: String): FieldBuilder {

            this.placeholder = StringFunctions.stripToNull(placeholder) { FormPlaceholderText(it) }
            return this

        }


        fun and(): EntityCreateHtmlFormDefBuilder {

            return this.enclosingBuilder

        }


        fun withHtmlInputType(htmlInputType: HtmlInputType): FieldBuilder {

            this.htmlInputType = htmlInputType
            return this

        }


    }


}
