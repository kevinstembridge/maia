package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.MaiaGenConstants
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class IndexSearchResultResponseDtoRenderer : AbstractTypescriptRenderer() {


    init {

        addImport(TypescriptImport("TotalHits", "./TotalHits"))

    }


    override fun renderSourceBody() {

        append("""
            |
            |export interface IndexSearchResult<T> {
            |    hits: T[];
            |    firstResultIndex: number;
            |    lastResultIndex: number;
            |    totalHits: TotalHits;
            |}
            |""".trimMargin())

    }


    override fun renderedFilePath(): String {

        return MaiaGenConstants.INDEX_SEARCH_RESULT_RENDERED_FILE_PATH

    }


}
