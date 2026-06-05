package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityCreatePageDef


class EntityCreatePageHtmlRenderer(
    private val entityCreatePageDef: EntityCreatePageDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityCreatePageDef.createPageAngularComponentNames.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="${entityCreatePageDef.pageTitle}" dataPageId="${entityCreatePageDef.dataPageId}">
            |    <div class="edit-form-container">
            |        <${entityCreatePageDef.createFormAngularComponentNames.componentSelector}
            |            (onSave)="onSaveClicked()"
            |            (onCancel)="onCancelClicked()"
            |        />
            |    </div>
            |</app-page-layout>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
