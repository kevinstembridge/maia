package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class UserSummaryDtoRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    init {

        addImport(authoritiesDef.enumDef.typescriptImport)

    }


    override fun renderedFilePath(): String {

        return authoritiesDef.userSummaryDtoRenderedFilePath

    }


    override fun renderSourceBody() {

        val authorityUqcn = authoritiesDef.enumDef.uqcn

        append(
            $$"""
            |
            |export interface UserSummaryDto {
            |    firstName: string;
            |    grantedAuthorities: $${authorityUqcn}[];
            |    id: string;
            |    lastName: string;
            |}
            |""".trimMargin()
        )

    }


}
