package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityEditPageDef


class EntityEditFormPageHtmlRenderer(
    private val entityEditPageDef: EntityEditPageDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityEditPageDef.editFormPageAngularComponentNames.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="${entityEditPageDef.pageTitle}" dataPageId="${entityEditPageDef.dataPageId}">
            |    @if (entityId(); as id) {
            |        <${entityEditPageDef.editFormAngularComponentNames.componentSelector}
            |            [entityId]="id"
            |            (onSave)="onSaveClicked()"
            |            (onCancel)="onCancelClicked()"
            |        />
            |    }
            |</app-page-layout>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
