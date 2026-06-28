package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.lang.text.StringFunctions


class TimelineBlotterPageHtmlRenderer(
    private val def: TimelineBlotterDef
) : AbstractSourceFileRenderer() {


    private val dataPageId = StringFunctions.toSnakeCase(def.timelineBlotterBaseName)

    private val blotterSelector = def.blotterComponentNames.componentSelector


    override fun renderedFilePath(): String {
        return def.blotterPageComponentNames.htmlRenderedFilePath
    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="${def.pageTitle}" dataPageId="${dataPageId}">
            |    @if (entityId(); as id) {
            |        <${blotterSelector} [entityId]="id" />
            |    }
            |</app-page-layout>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
