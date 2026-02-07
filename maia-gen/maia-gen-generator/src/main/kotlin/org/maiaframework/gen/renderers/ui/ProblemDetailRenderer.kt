package org.maiaframework.gen.renderers.ui

class ProblemDetailRenderer : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return "app/gen-components/common/model/ProblemDetail.ts"

    }


    override fun renderSourceBody() {

        append("""
            |
            |export interface ProblemDetail {
            |
            |    type: string;
            |    title: string;
            |    status: number;
            |    detail: string;
            |    instance: string;
            |
            |}""".trimMargin())

    }


}
