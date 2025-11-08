package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType

class EsDocMapperRenderer(private val esDocDef: EsDocDef): AbstractKotlinRenderer(esDocDef.esDocMapperClassDef) {

    private val esDocUqcn = esDocDef.uqcn


    init {

        esDocDef.dtoDef.allFieldsSorted.forEach { field ->

            val fieldType = field.fieldType

            if (fieldType is EsDocFieldType) {
                val mapperFqcn = fieldType.esDocDef.esDocMapperClassDef.fqcn
                addConstructorArg(ClassFieldDef.aClassField(mapperFqcn.uqcn.firstToLower(), mapperFqcn).privat().build())
            }

        }

    }


    override fun renderFunctions() {

        renderFunction_mapEsDoc()

    }


    private fun renderFunction_mapEsDoc() {

        blankLine()
        blankLine()
        appendLine("    override fun mapEsDoc(sourceMap: Map<String, Any?>): ${this.esDocUqcn} {")
        blankLine()
        appendLine("        return ${this.esDocUqcn}(")

        this.esDocDef.dtoDef.allFieldsSorted.forEach { classFieldDef ->

            val fieldType = classFieldDef.fieldType

            addImportFor(fieldType)

            val type = classFieldDef.unWrapIfComplexType().unqualifiedToString
            val questionMark = if (classFieldDef.nullable) "?" else ""

            when (fieldType) {
                is BooleanFieldType -> renderForPlainField(classFieldDef, questionMark)
                is BooleanTypeFieldType -> TODO("YAGNI?")
                is BooleanValueClassFieldType -> TODO("YAGNI?")
                is DataClassFieldType -> TODO("YAGNI?")
                is DomainIdFieldType -> renderForDomainId(classFieldDef.classFieldName)
                is DoubleFieldType -> renderForPlainField(classFieldDef, questionMark)
                is EnumFieldType -> renderForEnum(classFieldDef, questionMark, type)
                is EsDocFieldType -> renderForEsDoc(fieldType, classFieldDef, questionMark)
                is ForeignKeyFieldType -> TODO("$esDocUqcn $classFieldDef")
                is FqcnFieldType -> TODO("$esDocUqcn $classFieldDef")
                is IdAndNameFieldType -> TODO("YAGNI?")
                is InstantFieldType -> TODO("YAGNI?")
                is IntFieldType -> renderForPlainField(classFieldDef, questionMark)
                is IntTypeFieldType -> TODO("YAGNI?")
                is IntValueClassFieldType -> TODO("YAGNI?")
                is ListFieldType -> TODO("YAGNI?")
                is LocalDateFieldType -> TODO("YAGNI?")
                is LongFieldType -> renderForPlainField(classFieldDef, questionMark)
                is LongTypeFieldType -> TODO("YAGNI?")
                is MapFieldType -> TODO("YAGNI?")
                is ObjectIdFieldType -> TODO("YAGNI?")
                is PeriodFieldType -> TODO("YAGNI?")
                is RequestDtoFieldType -> TODO("YAGNI?")
                is SetFieldType -> TODO("YAGNI?")
                is SimpleResponseDtoFieldType -> TODO("YAGNI?")
                is StringFieldType -> renderForPlainField(classFieldDef, questionMark)
                is StringTypeFieldType -> renderForValueWrapper(fieldType, classFieldDef, questionMark)
                is StringValueClassFieldType -> renderForValueWrapper(fieldType, classFieldDef, questionMark)
                is UrlFieldType -> renderForPlainField(classFieldDef, questionMark)
            }

        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun renderForPlainField(classFieldDef: ClassFieldDef, questionMark: String) {

        appendLine("            sourceMap[\"${classFieldDef.classFieldName}\"] as$questionMark ${classFieldDef.fieldType.unwrap().uqcn},")

    }


    private fun renderForDomainId(fieldName: ClassFieldName) {

        appendLine("            DomainId(sourceMap[\"${fieldName}\"] as String),")

    }


    private fun renderForValueWrapper(
        fieldType: FieldType,
        classFieldDef: ClassFieldDef,
        questionMark: String
    ) {

        addImportFor(fieldType)

        if (classFieldDef.nullable) {
            appendLine("            (sourceMap[\"${classFieldDef.classFieldName}\"] as$questionMark ${fieldType.unwrap().uqcn})?.let { ${fieldType.uqcn}(it) },")
        } else {
            appendLine("            ${classFieldDef.fieldType.uqcn}(sourceMap[\"${classFieldDef.classFieldName}\"] as$questionMark ${fieldType.unwrap().uqcn}),")
        }

    }


    private fun renderForEnum(classFieldDef: ClassFieldDef, questionMark: String, type: String) {

        if (classFieldDef.nullable) {
            appendLine("            (sourceMap[\"${classFieldDef.classFieldName}\"] as$questionMark $type)?.let { ${classFieldDef.fqcn.uqcn}.valueOf(it) },")
        } else {
            appendLine("            ${classFieldDef.unqualifiedToString}.valueOf(sourceMap[\"${classFieldDef.classFieldName}\"] as$questionMark $type),")
        }

    }


    private fun renderForEsDoc(
        fieldType: EsDocFieldType,
        classFieldDef: ClassFieldDef,
        questionMark: String
    ) {

        val esDocDef = fieldType.esDocDef
        val esDocMapperForField = "this.${esDocDef.esDocMapperClassDef.uqcn.firstToLower()}"
        addImportFor(fieldType)
        addImportFor(esDocDef.esDocMapperClassDef.fqcn)

        if (classFieldDef.nullable) {
            appendLine("            (sourceMap[\"${classFieldDef.classFieldName}\"] as$questionMark Map<String, Any?>)?.let { $esDocMapperForField.mapEsDoc(it) },")
        } else {
            appendLine("            $esDocMapperForField.mapEsDoc(sourceMap[\"${classFieldDef.classFieldName}\"] as Map<String, Any?>),")
        }

    }


}
