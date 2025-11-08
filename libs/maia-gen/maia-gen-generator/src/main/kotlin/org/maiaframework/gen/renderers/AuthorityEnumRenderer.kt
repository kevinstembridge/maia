package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class AuthorityEnumRenderer(private val authoritiesDef: AuthoritiesDef) : EnumRenderer(authoritiesDef.enumDef) {


    override fun renderInnerClasses() {

        blankLine()
        blankLine()
        appendLine("    object Values {")
        blankLine()

        this.authoritiesDef.enumDef.enumValueDefs.forEach { enumValueDef ->
            appendLine("        const val ${enumValueDef.name} = \"${enumValueDef.name}\"")
        }

        blankLine()
        appendLine("    }")

    }


}
