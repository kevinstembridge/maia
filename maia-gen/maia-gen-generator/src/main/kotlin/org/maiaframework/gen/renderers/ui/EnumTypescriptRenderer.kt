package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EnumDef

open class EnumTypescriptRenderer(private val enumDef: EnumDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.enumDef.renderedTypescriptFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export enum ${this.enumDef.uqcn} {")

        this.enumDef.enumValueDefs.sortedBy { it.name }.forEach { enumValueDef ->

            blankLine()
            blankLine()

            enumValueDef.description?.let {
                appendLine("    // ${it.value.replace("\n", "\n    // ")}")
            }

            appendLine("    ${enumValueDef.name} = '${enumValueDef.name}',")
        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}
