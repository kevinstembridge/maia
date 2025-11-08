package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


open class EnumRenderer(private val enumDef: EnumDef) : AbstractKotlinRenderer(enumDef.classDef) {


    init {

        if (enumDef.hasDisplayName) {
            addConstructorArg(aClassField("displayName", Fqcns.STRING).build())
        }

    }


    override fun renderPreClassFields() {

        val itr = this.enumDef.enumValueDefs.iterator()

        while (itr.hasNext()) {

            val enumValueDef = itr.next()

            blankLine()
            blankLine()

            if (enumValueDef.description != null) {

                append("""
                    |    /**
                    |     * ${enumValueDef.description}
                    |     */
                    |""".trimMargin())

            }

            append("    ${enumValueDef.name}")

            enumValueDef.displayName?.let { displayName -> append("(\"$displayName\")") }

            if (itr.hasNext()) {
                append(",")
            } else {
                append(";")
            }

            newLine()

        }

    }


    override fun renderFunctions() {

        // do nothing

    }


}
