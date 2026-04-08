package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ConstructorArg


class DtoRenderer(classDef: ClassDef) : AbstractKotlinRenderer(classDef) {


    init {
        this.classDef.allFieldsSorted.forEach { classField ->
            addConstructorArg(ConstructorArg(classField))
            addImportFor(classField.fieldType)
        }

    }


    override fun renderFunctions() {

        `render function toString`()

    }


    private fun `render function toString`() {

        if (this.classDef.allFieldsSorted.none { it.isMasked }) {
            return
        }

        append("""
            |
            |
            |    override fun toString(): String {
            |
            |        return "${this.classDef.uqcn}{" +
            |                """.trimMargin())

        val lines = this.classDef.allFieldsSorted.map { fd ->

            val classFieldName = fd.classFieldName

            if (fd.isMasked) {
                "\"$classFieldName = 'MASKED'\" +"
            } else {
                "\"$classFieldName = '\" + this.$classFieldName + '\\'' +"
            }

        }.joinToString(separator = " \", \" + \n                ")

        append(lines)

        append("""
            |
            |                "}";
            |
            |    }""".trimMargin())

    }


}
