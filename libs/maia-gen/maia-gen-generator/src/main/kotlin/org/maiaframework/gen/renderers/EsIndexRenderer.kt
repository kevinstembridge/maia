package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.ElasticIndexBaseName
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class EsIndexRenderer(esIndexClassDef: ClassDef, private val elasticIndexBaseName: ElasticIndexBaseName) : AbstractKotlinRenderer(esIndexClassDef) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("esIndexNameLookup", Fqcns.ES_INDEX_NAME_LOOKUP).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.ES_INDEX_BASE_NAME)

        blankLine()
        blankLine()
        appendLine("    private val indexBaseName = EsIndexBaseName(\"${this.elasticIndexBaseName}\")")

    }


    override fun renderFunctions() {

        renderFunction_indexName()

    }


    private fun renderFunction_indexName() {

        addImportFor(Fqcns.ES_INDEX_NAME)

        blankLine()
        blankLine()
        appendLine("    fun indexName(): EsIndexName {")
        blankLine()
        appendLine("        return this.esIndexNameLookup.indexName(indexBaseName)")
        blankLine()
        appendLine("    }")

    }


}
