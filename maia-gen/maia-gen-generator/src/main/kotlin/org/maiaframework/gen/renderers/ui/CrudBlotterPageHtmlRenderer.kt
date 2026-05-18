package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.CrudBlotterPageDef

class CrudBlotterPageHtmlRenderer(
    private val crudBlotterPageDef: CrudBlotterPageDef
) : AbstractSourceFileRenderer() {

    override fun renderedFilePath(): String {
        return crudBlotterPageDef.pageAngularComponentNames.htmlRenderedFilePath
    }

    override fun renderSource(): String {
        append("""
            |<app-page-layout pageTitle="${crudBlotterPageDef.pageTitle}" dataPageId="${crudBlotterPageDef.dataPageId}">
            |    <${crudBlotterPageDef.crudBlotterSelector}></${crudBlotterPageDef.crudBlotterSelector}>
            |</app-page-layout>
            |""".trimMargin())
        return sourceCode.toString()
    }

}
