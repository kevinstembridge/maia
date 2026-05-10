package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ConstructorArg
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
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
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType


class EntityPkRenderer(
    entityDef: EntityDef
) : AbstractKotlinRenderer(
    entityDef.entityPkClassDef
) {


    private val pkFields = this.classDef.allFields


    init {

        val constructorArgs = this.classDef.allFields.map { ConstructorArg(it) }
        setConstructorArgs(constructorArgs)

    }


    override fun renderPreClassFields() {

        addImportRaw("java.net.URLEncoder")

        append("""
            |
            |
            |    val encoded: String by lazy {
            |
            |        listOf(${pkFields.joinToString(", ") { it.classFieldName.toString() }}).joinToString(":") { URLEncoder.encode(it.toString(), "UTF-8") }
            |
            |    }
            |""".trimMargin())

    }


    override fun renderCompanionObject() {

        addImportRaw("java.net.URLDecoder")

        append("""
            |    companion object {
            |
            |
            |        fun from(pk: String): ${classDef.uqcn} {
            |
            |            val parts = pk.split(":")
            |""".trimMargin())

        pkFields.forEachIndexed { i, field ->
            appendLine("            val ${field.classFieldName} = ${parseExpression(field, i)}")
        }

        append("""
            |
            |            return ${classDef.uqcn}(${pkFields.joinToString(", ") { it.classFieldName.toString() }})
            |
            |        }
            |
            |
            |    }
            |
            |
            |""".trimMargin())

    }


    private fun `render function toString`() {

        addImportRaw("java.net.URLEncoder")

        append("""
            |
            |
            |    override fun toString(): String {
            |
            |        return listOf(${pkFields.joinToString(", ") { it.classFieldName.toString() }})
            |            .joinToString(":") { URLEncoder.encode(it.toString(), "UTF-8") }
            |
            |    }
            |""".trimMargin())

    }


    private fun parseExpression(field: ClassFieldDef, index: Int): String {

        val decoded = "URLDecoder.decode(parts[$index], \"UTF-8\")"

        return when (val fieldType = field.fieldType) {
            is BooleanFieldType -> TODO("YAGNI?")
            is BooleanTypeFieldType -> TODO("YAGNI?")
            is BooleanValueClassFieldType -> TODO("YAGNI?")
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> "DomainId($decoded)"
            is DoubleFieldType -> TODO("YAGNI?")
            is EnumFieldType -> TODO("YAGNI?")
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> TODO("YAGNI?")
            is FqcnFieldType -> TODO("YAGNI?")
            is InstantFieldType -> TODO("YAGNI?")
            is IntFieldType, is IntTypeFieldType -> "$decoded.toInt()"
            is IntValueClassFieldType -> TODO("YAGNI?")
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> TODO("YAGNI?")
            is LongFieldType, is LongTypeFieldType -> "$decoded.toLong()"
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> TODO("YAGNI?")
            is PeriodFieldType -> TODO("YAGNI?")
            is PkAndNameFieldType -> TODO("YAGNI?")
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> decoded
            is StringTypeFieldType -> TODO("YAGNI?")
            is StringValueClassFieldType -> "${fieldType.uqcn}($decoded)"
            is UrlFieldType -> TODO("YAGNI?")
        }

    }


}
