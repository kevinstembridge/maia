package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DataClassDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isValueFieldWrapper
import java.util.*


class DataClassRenderer(private val dataClassDef: DataClassDef) : AbstractKotlinRenderer(getClassDefForRenderer(dataClassDef)) {


    init {
        this.dataClassDef.fields
                .forEach { classField ->
                    addConstructorArg(classField.classFieldDef)
                    addImportFor(classField.fieldType)
                }

    }


    override fun renderFunctions() {

//        renderGetters()
//        renderCopyMethods()
        renderMethod_toString()
//        renderEqualsAndHashCode(this.dataClassDef.classFieldDefs)

    }


    private fun renderGetters() {

        this.dataClassDef.classFieldDefs.forEach { this.renderGetterFor(it) }

    }


    private fun renderCopyMethods() {

        this.dataClassDef.classFieldDefs.forEach { this.renderCopyMethod(it) }

    }


    private fun renderCopyMethod(classFieldDef: ClassFieldDef) {

        blankLine()
        blankLine()
        appendLine("    public %s with%s(final %s %s) {",
                this.dataClassDef.classDef.uqcn,
                classFieldDef.classFieldName.firstToUpper(),
                classFieldDef.fieldType.unqualifiedToString,
                classFieldDef.classFieldName)

        renderCallToConstructor()

        appendLine("    }")

    }


    private fun renderCallToConstructor() {

        blankLine()
        append("        return new %s(", this.dataClassDef.uqcn)

        val constructorArgs = this.dataClassDef.classFieldDefs
                .map { fieldDef -> "\n                " + fieldDef.classFieldName }
                .joinToString(",")

        append(constructorArgs)
        append(");")
        newLine()

    }


    private fun renderGetterFor(fieldDef: ClassFieldDef) {

        blankLine()
        blankLine()
        appendLine("    public %s %s() {", fieldDef.unqualifiedToString, fieldDef.getterMethodName)
        blankLine()

        if (fieldDef.isList) {
            addImportFor(ArrayList::class.java)
            appendLine("        return new ArrayList<>(this.%s);", fieldDef.classFieldName)
        } else if (fieldDef.fieldType.isValueFieldWrapper()) {
            appendLine("        return new %s(this.%s);", fieldDef.unqualifiedToString, fieldDef.classFieldName)
        } else {
            appendLine("        return this.%s;", fieldDef.classFieldName)
        }

        blankLine()
        appendLine("    }")

    }


    private fun renderMethod_toString() {

        if (this.dataClassDef.classFieldDefs.none { it.isMasked }) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    override fun toString(): String {")
        blankLine()
        appendLine("        return \"${this.dataClassDef.uqcn}{\" +")
        append("                ")

        val lines: String = this.dataClassDef.classFieldDefs.map { fd ->

            if (fd.isMasked) {
                "\"${fd.classFieldName} = 'MASKED'\" +"
            } else {
                "\"${fd.classFieldName} = '\" + this.${fd.classFieldName} + '\\'' +"
            }

        }.joinToString(" \", \" + \n                ")

        append(lines)

        appendLine("\n                \"}\"")
        blankLine()
        appendLine("    }")

    }


    companion object {


        private fun getClassDefForRenderer(dataClassDef: DataClassDef): ClassDef {

            val classDef = dataClassDef.classDef

            return if (dataClassDef.isWithHandcodedSubclass) {

                classDef.withUqcnPrefix("Abstract").withAbstract(true)

            } else {

                classDef

            }

        }

    }

}
