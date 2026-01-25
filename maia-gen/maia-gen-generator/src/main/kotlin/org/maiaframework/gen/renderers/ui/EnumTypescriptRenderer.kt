package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EnumDef

class EnumTypescriptRenderer(private val enumDef: EnumDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.enumDef.renderedTypescriptFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export enum ${this.enumDef.uqcn} {")

        this.enumDef.enumValueDefs.sortedBy { it.name }.forEach { enumValueDef ->

            blankLine()
            blankLine()

            if (enumValueDef.description != null) {

                appendLine("    // ${enumValueDef.description}")

            }

            appendLine("    ${enumValueDef.name} = '${enumValueDef.name}',")
        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}
