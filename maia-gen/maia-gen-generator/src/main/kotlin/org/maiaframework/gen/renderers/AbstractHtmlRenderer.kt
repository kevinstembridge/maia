package org.maiaframework.gen.renderers

abstract class AbstractHtmlRenderer(private val filePath: String): AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.filePath

    }


}
