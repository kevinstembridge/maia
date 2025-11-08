package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import java.util.Locale


class StringTypeRenderer(stringTypeDef: StringTypeDef) : AbstractKotlinRenderer(stringTypeDef.classDef) {

    private val caseMode: StringTypeDef.CaseMode = stringTypeDef.caseMode


    init {

        addConstructorArg(ClassFieldDef.aClassField("value", stringTypeDef.simpleTypeUnderlyingFieldType).build())

    }


    override fun renderCallToSuperConstructor(superclassDef: ClassDef) {

        append("(")

        val inheritedField = superclassDef.allFieldsSorted.first()

        append("${inheritedField.classFieldName}")

        when (this.caseMode) {
            StringTypeDef.CaseMode.ALWAYS_LOWER -> {
                addImportFor<Locale>()
                append(".lowercase(Locale.getDefault())")
            }
            StringTypeDef.CaseMode.ALWAYS_UPPER -> {
                addImportFor<Locale>()
                append(".uppercase(Locale.getDefault())")
            }
            StringTypeDef.CaseMode.AS_PROVIDED -> append("")
        }

        append(")")
        newLine()

    }


}
