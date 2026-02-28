package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class SigninRequestDtoRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return authoritiesDef.loginRequestDtoRenderedFilePath

    }


    override fun renderSourceBody() {

        append(
            """
            |
            |export interface LoginRequestDto {
            |    emailAddress: string;
            |    password: string;
            |}
            |""".trimMargin()
        )

    }


}
