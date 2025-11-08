package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocFieldNameMapperRenderer
import org.maiaframework.gen.renderers.EsIndexControlRenderer
import org.maiaframework.gen.renderers.EsIndexMetaClassRenderer
import org.maiaframework.gen.renderers.EsIndexRenderer
import org.maiaframework.gen.spec.definition.ModelDef


class ElasticSearchModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
): AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        renderEsIndexes()
        renderTypeaheadEsIndexes()
        renderEsDocMetaClasses()
        renderEsDocFieldNameMappers()

    }


    private fun renderEsIndexes() {

        this.modelDef.esDocsDefs.forEach { esDocDef ->
            EsIndexRenderer(esDocDef.esIndexClassDef, esDocDef.elasticIndexBaseName).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderTypeaheadEsIndexes() {

        this.modelDef.typeaheadDefs.forEach { typeaheadDef ->
            EsIndexRenderer(typeaheadDef.esIndexClassDef, typeaheadDef.elasticIndexBaseName).renderToDir(this.kotlinOutputDir)
            EsIndexMetaClassRenderer(typeaheadDef.esDocDef).renderToDir(this.kotlinOutputDir)
            EsIndexControlRenderer(typeaheadDef.esDocDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderEsDocMetaClasses() {

        this.modelDef.esDocsDefs.forEach { esDocDef ->
            EsIndexMetaClassRenderer(esDocDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderEsDocFieldNameMappers() {

        this.modelDef.dtoHtmlTableDefs.filter { it.dtoHtmlTableSourceDef.esDocDef != null }.forEach { dtoHtmlTableDef ->
            EsDocFieldNameMapperRenderer(dtoHtmlTableDef).renderToDir(this.kotlinOutputDir)
        }

    }


}
