package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailViewPageHtmlRenderer(
    private val entityDetailViewDef: EntityDetailViewDef,
    private val blotterPageDef: BlotterPageDef?,
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityDetailViewDef.viewPageComponentHtmlRenderedFilePath

    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="${entityDetailViewDef.pageTitle}" dataPageId="${entityDetailViewDef.dataPageId}">
            |    @if (entityId(); as id) {
            |        <${entityDetailViewDef.viewContentAngularComponentNames.componentSelector} [entityId]="id" />
            |    }
            |""".trimMargin())

        blotterPageDef?.let {
            append("""
                |    <button matButton aria-label="Blotter" (click)="onBlotterClicked()">
                |        <mat-icon>list</mat-icon>
                |        Go to Blotter
                |    </button>
                |""".trimMargin())
        }

        append("""
            |    @if (canEdit && entityId()) {
            |        <button matButton aria-label="Edit" (click)="onEditClicked()">
            |            <mat-icon>edit</mat-icon>
            |            Edit
            |        </button>
            |    }
            |</app-page-layout>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
