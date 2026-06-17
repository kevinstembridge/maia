package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocFieldNameMapperRenderer
import org.maiaframework.gen.renderers.EsIndexControlRenderer
import org.maiaframework.gen.renderers.EsIndexMetaClassRenderer
import org.maiaframework.gen.renderers.EsIndexRenderer
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = ElasticSearchModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ElasticSearchModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        renderEsIndexes()
        renderTypeaheadEsIndexes()
        renderEsDocMetaClasses()
        renderEsDocFieldNameMappers()

    }


    private fun renderEsIndexes() {

        this.applicationModelDef.esDocsDefs.forEach { esDocDef ->
            EsIndexRenderer(esDocDef.esIndexClassDef, esDocDef.elasticIndexBaseName).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderTypeaheadEsIndexes() {

        this.applicationModelDef.typeaheadDefs.forEach { typeaheadDef ->
            EsIndexRenderer(typeaheadDef.esIndexClassDef, typeaheadDef.elasticIndexBaseName).renderToDir(this.kotlinOutputDir)
            EsIndexMetaClassRenderer(typeaheadDef.esDocDef).renderToDir(this.kotlinOutputDir)
            EsIndexControlRenderer(typeaheadDef.esDocDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderEsDocMetaClasses() {

        this.applicationModelDef.esDocsDefs.forEach { esDocDef ->
            EsIndexMetaClassRenderer(esDocDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderEsDocFieldNameMappers() {

        this.applicationModelDef.blotterDefs
            .filter { it.blotterSourceDef is BlotterEsDocSourceDef }
            .forEach { blotterDef ->
                EsDocFieldNameMapperRenderer(blotterDef).renderToDir(this.kotlinOutputDir)
            }

    }


}
