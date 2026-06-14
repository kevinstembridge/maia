package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.lang.text.StringFunctions


class EntityHistoryBlotterPageHtmlRenderer(
    private val def: EntityHistoryBlotterDef,
    viewPageDef: EntityDetailViewDef?
) : AbstractSourceFileRenderer() {


    private val dataPageId = StringFunctions.toSnakeCase(def.historyBlotterBaseName)

    private val blotterSelector = def.blotterComponentNames.componentSelector

    private val showsViewButton = def.isJoinEntityHistory == false && viewPageDef != null


    override fun renderedFilePath(): String {
        return def.blotterPageComponentNames.htmlRenderedFilePath
    }


    override fun renderSource(): String {

        if (def.isJoinEntityHistory) {

            append("""
                |<app-page-layout pageTitle="${def.pageTitle}" dataPageId="${dataPageId}">
                |    <${blotterSelector} />
                |</app-page-layout>
                |""".trimMargin())

        } else if (showsViewButton) {

            append("""
                |<app-page-layout pageTitle="${def.pageTitle}" dataPageId="${dataPageId}">
                |    @if (entityId(); as id) {
                |        <${blotterSelector} [entityId]="id" />
                |    }
                |    <button matButton aria-label="View" (click)="onViewClicked()">
                |        <mat-icon>visibility</mat-icon>
                |        View
                |    </button>
                |</app-page-layout>
                |""".trimMargin())

        } else {

            append("""
                |<app-page-layout pageTitle="${def.pageTitle}" dataPageId="${dataPageId}">
                |    @if (entityId(); as id) {
                |        <${blotterSelector} [entityId]="id" />
                |    }
                |</app-page-layout>
                |""".trimMargin())

        }

        return sourceCode.toString()

    }


}
