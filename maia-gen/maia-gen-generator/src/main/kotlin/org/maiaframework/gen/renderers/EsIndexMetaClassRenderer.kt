package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BooleanEsDocMappingType
import org.maiaframework.gen.spec.definition.DateEsDocMappingType
import org.maiaframework.gen.spec.definition.DoubleEsDocMappingType
import org.maiaframework.gen.spec.definition.EntityRefDef
import org.maiaframework.gen.spec.definition.EntityRefEsDocMappingType
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.EsDocFieldDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.KeywordEsDocMappingType
import org.maiaframework.gen.spec.definition.LongEsDocMappingType
import org.maiaframework.gen.spec.definition.ObjectEsDocMappingType
import org.maiaframework.gen.spec.definition.ScaledFloatEsDocMappingType
import org.maiaframework.gen.spec.definition.SearchAsYouTypeEsDocMappingType
import org.maiaframework.gen.spec.definition.TextAndKeywordEsDocMappingType
import org.maiaframework.gen.spec.definition.TextEsDocMappingType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType

class EsIndexMetaClassRenderer(private val esDocDef: EsDocDef) : AbstractKotlinRenderer(esDocDef.esDocMetaClassDef) {


    override fun renderPreClassFields() {

        addImportFor(Fqcns.ES_INDEX_BASE_NAME)
        addImportFor(Fqcns.ES_INDEX_BASE_NAME_AND_VERSION)
        addImportFor(Fqcns.ES_INDEX_VERSION)
        addImportFor(Fqcns.ES_TYPE_MAPPING)
        addImportFor(Fqcns.ES_PROPERTY)

        append("""
            |
            |
            |    val indexBaseName = IndexBaseName("${this.esDocDef.elasticIndexBaseName}")
            |
            |
            |    val indexVersion = IndexVersion(${this.esDocDef.esDocVersion})
            |
            |
            |    val indexBaseNameAndVersion = IndexBaseNameAndVersion(indexBaseName, indexVersion)
            |
            |
            |    const val indexDescription = "${this.esDocDef.indexDescription}"
            |
            |
            |    val typeMapping = TypeMapping.of { m ->
            |""".trimMargin())

        this.esDocDef.fields.forEach { esDocFieldDef ->

            val fieldType = esDocFieldDef.classFieldDef.fieldType

            if (fieldType is EsDocFieldType) {

                val nestedEsDocDef = fieldType.esDocDef
                val fieldName = esDocFieldDef.classFieldDef.classFieldName

                nestedEsDocDef.fields.forEach { docFieldDef ->
                    val fieldPath = "${fieldName}.${docFieldDef.classFieldDef.classFieldName}"
                    renderProperty(fieldPath, docFieldDef)
                }

            } else {

                val fieldPath = esDocFieldDef.classFieldDef.classFieldName.value
                renderProperty(fieldPath, esDocFieldDef)

            }

        }

        appendLine("    }")

    }


    private fun renderProperty(fieldPath: String, esDocFieldDef: EsDocFieldDef) {

        val mappingType = esDocFieldDef.mappingType

        when (mappingType) {
            is BooleanEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is DateEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is DoubleEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is KeywordEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is LongEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is EntityRefEsDocMappingType -> renderEntityRefProperties(fieldPath, mappingType)
            is ObjectEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is ScaledFloatEsDocMappingType -> appendScaledFloatLine(fieldPath, mappingType)
            is SearchAsYouTypeEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is TextAndKeywordEsDocMappingType -> renderPropertyWithRawKeywordField(fieldPath)
            is TextEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
        }

    }


    private fun appendLineFor(fieldName: String, mappingType: EsDocMappingType) {

        // the ES client's builder method for some types is a Kotlin keyword (e.g. object), so it must be backtick-escaped
        val builderMethodName = if (mappingType is ObjectEsDocMappingType) "`$mappingType`" else "$mappingType"

        appendLine("            m.properties(\"$fieldName\", Property.of { p -> p.$builderMethodName { it } })")

    }


    private fun appendScaledFloatLine(fieldName: String, mappingType: ScaledFloatEsDocMappingType) {

        appendLine("            m.properties(\"$fieldName\", Property.of { p -> p.scaledFloat { it.scalingFactor(${mappingType.scalingFactor}.0) } })")

    }


    private fun renderEntityRefProperties(fieldName: String, mappingType: EntityRefEsDocMappingType) {

        appendLine("            m.properties(\"$fieldName.id\", Property.of { p -> p.keyword { it } })")
        renderPropertyWithRawKeywordField("$fieldName.${EntityRefDef.DISPLAY_NAME_FIELD}")

        if (mappingType.hasShortcode) {
            appendLine("            m.properties(\"$fieldName.shortcode\", Property.of { p -> p.keyword { it } })")
        }

    }


    private fun renderPropertyWithRawKeywordField(fieldName: String) {

        append("""
            |            m.properties("$fieldName") {
            |                propertyBuilder -> propertyBuilder.text {
            |                    t -> t.fields(mapOf("${EntityRefDef.RAW_SUB_FIELD}" to Property.of { p -> p.keyword { it } }))
            |                }
            |            }
            |""".trimMargin())

    }


}
