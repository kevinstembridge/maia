package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef

object EntityRendererHelper {


    fun renderCallToEntityConstructor(entity: EntityDef, indentSize: Int, renderer: AbstractSourceRenderer) {

        val indent = "".padEnd(indentSize, ' ')

        renderer.blankLine()
        renderer.append(indent + "return ${entity.entityUqcn}(")

        val constructorArgs = entity.allEntityFieldsSorted.joinToString(",") { fieldDef ->
            "\n        " + indent + fieldDef.classFieldDef.classFieldName
        }

        renderer.append(constructorArgs)
        renderer.append(")")
        renderer.newLine()

    }


}
