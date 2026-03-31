package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class EsIndexControlRenderer(private val esDocDef: EsDocDef) : AbstractKotlinRenderer(esDocDef.esIndexControlClassDef) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("esIndexNameProvider", Fqcns.ES_INDEX_NAME_OVERRIDER).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("esIndexActiveVersionManager", Fqcns.ES_INDEX_ACTIVE_VERSION_MANAGER).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("client", Fqcns.ELASTIC_CLIENT).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.ES_INDEX_BASE_NAME)
        addImportFor(Fqcns.ES_TYPE_MAPPING)

        append("""
            |
            |
            |    override val indexName = ${this.esDocDef.esDocMetaClassDef.uqcn}.indexName")
            |
            |
            |    override val indexDescription = ${this.esDocDef.esDocMetaClassDef.uqcn}.indexDescription")
            |
            |
            |    override val typeMapping: TypeMapping = ${this.esDocDef.esDocMetaClassDef.uqcn}.typeMapping")
            |""".trimMargin())

    }


}
