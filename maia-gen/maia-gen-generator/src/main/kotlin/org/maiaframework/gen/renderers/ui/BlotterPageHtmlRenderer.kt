package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.BlotterPageDef


class BlotterPageHtmlRenderer(
    private val blotterPageDef: BlotterPageDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return blotterPageDef.pageAngularComponentNames.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="${blotterPageDef.pageTitle}" dataPageId="${blotterPageDef.dataPageId}">
            |    <${blotterPageDef.blotterComponentSelector}></${blotterPageDef.blotterComponentSelector}>
            |</app-page-layout>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
