package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType

class EntityDetailViewPageHtmlRenderer(private val entityDetailViewDef: EntityDetailViewDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityDetailViewDef.viewPageComponentHtmlRenderedFilePath

    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="" dataPageId="TODO">
            |    @if (entityId(); as id) {
            |        <app-huh [entityId]="id" />
            |    }
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
