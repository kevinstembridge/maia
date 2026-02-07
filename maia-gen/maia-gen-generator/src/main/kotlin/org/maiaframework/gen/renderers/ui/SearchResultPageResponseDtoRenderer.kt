package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.MaiaGenConstants


class SearchResultPageResponseDtoRenderer : AbstractTypescriptRenderer() {


    override fun renderSourceBody() {

        append("""
            |
            |export interface SearchResultPage<T> {
            |    firstResultIndex: number;
            |    lastResultIndex: number;
            |    limit: number;
            |    offset: number;
            |    results: T[];
            |    totalResultCount: number;
            |}
            |""".trimMargin())

    }


    override fun renderedFilePath(): String {

        return MaiaGenConstants.SEARCH_RESULT_PAGE_RENDERED_FILE_PATH

    }


}
