package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.lang.AnnotationUsageSite
import org.maiaframework.gen.spec.definition.lang.ConstructorArg


class FormModelRenderer(private val formModelDef: FormModelDef) : AbstractKotlinRenderer(formModelDef.classDef) {


    init {
        this.formModelDef.allFields
                .forEach { classField ->
                    addConstructorArg(ConstructorArg(classField, classField.validationAnnotations))
                    addImportFor(classField.fieldType)
                }

    }


    override fun renderConstructorArgs(args: List<ConstructorArg>) {

        val separator = if (args.size > 1) {
            newLine()
            append("        ")
            ",\n        "
        } else {
            ", "
        }

        val textToRender = args
                .asSequence()
                .map { constructorArg ->

                    val classField = constructorArg.classFieldDef
                    val fieldIsNotNullable = constructorArg.classFieldDef.nullable == false

                    val usageSite = AnnotationUsageSite.field
                    val annotationString = constructorArg.annotationDefs.map { "${it.toStringInKotlin(usageSite)} " }.joinToString("")
                    val constructorArgName = if (fieldIsNotNullable) "${classField.classFieldName}" else classField.classFieldName
                    "${annotationString}var $constructorArgName: ${classField.unWrapIfComplexType().convertToNullable().unqualifiedToString} = null"

                }.joinToString(separator)

        append(textToRender)

    }


    override fun renderFunctions() {

        renderMethod_toString()

    }


    private fun renderMethod_toString() {

        blankLine()
        blankLine()
        appendLine("    override fun toString(): String {")
        blankLine()
        appendLine("        return \"%s{\" +", this.formModelDef.uqcn)
        append("                ")

        val lines: String = this.formModelDef.allFields
                .map { fd ->

                    if (fd.isMasked) {
                        String.format("\"%s = 'MASKED'\" +", fd.classFieldName)
                    } else {
                        String.format("\"%s = '\" + this.%s + '\\'' +", fd.classFieldName, fd.classFieldName)
                    }
                }.joinToString(" \", \" + \n                ")

        append(lines)

        appendLine("\n                \"}\"")
        blankLine()
        appendLine("    }")

    }


}
