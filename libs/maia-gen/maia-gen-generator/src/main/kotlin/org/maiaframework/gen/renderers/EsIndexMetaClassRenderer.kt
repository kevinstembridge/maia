package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BooleanEsDocMappingType
import org.maiaframework.gen.spec.definition.DateEsDocMappingType
import org.maiaframework.gen.spec.definition.DoubleEsDocMappingType
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.EsDocFieldDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.KeywordEsDocMappingType
import org.maiaframework.gen.spec.definition.LongEsDocMappingType
import org.maiaframework.gen.spec.definition.ObjectEsDocMappingType
import org.maiaframework.gen.spec.definition.SearchAsYouTypeEsDocMappingType
import org.maiaframework.gen.spec.definition.TextAndKeywordEsDocMappingType
import org.maiaframework.gen.spec.definition.TextEsDocMappingType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType

class EsIndexMetaClassRenderer(private val esDocDef: EsDocDef) : AbstractKotlinRenderer(esDocDef.esDocMetaClassDef) {


    override fun renderPreClassFields() {

        addImportFor(Fqcns.ES_INDEX_BASE_NAME)
        addImportFor(Fqcns.ES_INDEX_NAME)
        addImportFor(Fqcns.ES_INDEX_VERSION)
        addImportFor(Fqcns.ES_TYPE_MAPPING)
        addImportFor(Fqcns.ES_PROPERTY)

        appendLine("""
            |
            |    val indexBaseName = EsIndexBaseName("${this.esDocDef.elasticIndexBaseName}")
            |
            |    val indexName = EsIndexName(indexBaseName, EsIndexVersion(${this.esDocDef.esDocVersion}))
            |
            |    const val indexDescription = "${this.esDocDef.indexDescription}"
            |
            |    val typeMapping = TypeMapping.of { m ->""".trimMargin())

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

        when (esDocFieldDef.mappingType) {
            is BooleanEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is DateEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is DoubleEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is KeywordEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is LongEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is ObjectEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is SearchAsYouTypeEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
            is TextAndKeywordEsDocMappingType -> renderPropertyWithRawKeywordField(fieldPath)
            is TextEsDocMappingType -> appendLineFor(fieldPath, esDocFieldDef.mappingType)
        }

    }


    private fun appendLineFor(fieldName: String, mappingType: EsDocMappingType) {

        appendLine("            m.properties(\"$fieldName\", Property.of { p -> p.$mappingType { it } })")

    }


    private fun renderPropertyWithRawKeywordField(fieldName: String) {

        appendLine("""
            |            m.properties("$fieldName") {
            |                propertyBuilder -> propertyBuilder.text {
            |                    t -> t.fields(mapOf("raw" to Property.of { p -> p.keyword { it } }))
            |                }
            |            }""".trimMargin())

    }


}
