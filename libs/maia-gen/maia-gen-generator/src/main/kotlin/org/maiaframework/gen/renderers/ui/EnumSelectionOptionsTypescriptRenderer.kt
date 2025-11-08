package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EnumDef

class EnumSelectionOptionsTypescriptRenderer(private val enumDef: EnumDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.enumDef.selectOptionsRenderedTypescriptFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |${this.enumDef.importStatement}
            |
            |export const ${this.enumDef.selectOptionsUqcn} = [
            |""".trimMargin())

        this.enumDef.enumValueDefs.forEach { enumValueDef ->

            append("""
                |    {
                |        name: ${this.enumDef.uqcn}.${enumValueDef.name},
                |        displayName: '${enumValueDef.displayNameNonNull}',
                |""".trimMargin())

            val descriptionValue = enumValueDef.description?.value

            if (descriptionValue != null) {

                val escapedDescription = descriptionValue.replace("\'", "\\\'")
                appendLine("        description: '${escapedDescription}'")

            }

            appendLine("    },")

        }

        appendLine("];")

    }


}
