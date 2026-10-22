package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocFieldNameMapperRenderer
import org.maiaframework.gen.renderers.EsIndexControlRenderer
import org.maiaframework.gen.renderers.EsIndexMetaClassRenderer
import org.maiaframework.gen.renderers.EsIndexRenderer
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.EsDocDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = ElasticSearchModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


/**
 * The EsDocs derived from searchable DTOs declared with `elasticsearchEnabled`. Manual `esDoc {}` EsDocs
 * are deliberately excluded because their control classes are hand written.
 */
fun derivedEsDocDefs(applicationModelDef: ApplicationModelDef): List<EsDocDef> {

    return applicationModelDef.searchableDtoDefs.flatMap { it.esDocDefs }

}


class ElasticSearchModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        renderTypeaheadEsIndexes()
        renderDerivedEsIndexControls()
        renderEsDocMetaClasses()
        renderEsDocFieldNameMappers()

    }


    private fun renderTypeaheadEsIndexes() {

        this.applicationModelDef.typeaheadDefs.forEach { typeaheadDef ->
            EsIndexRenderer(typeaheadDef.esIndexClassDef, typeaheadDef.elasticIndexBaseName).renderToDir(this.kotlinOutputDir)
            EsIndexMetaClassRenderer(typeaheadDef.esDocDef).renderToDir(this.kotlinOutputDir)
            EsIndexControlRenderer(typeaheadDef.esDocDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderDerivedEsIndexControls() {

        // the EsIndex and Meta classes of derived EsDocs are rendered with all esDocsDefs
        derivedEsDocDefs(this.applicationModelDef).forEach { esDocDef ->
            EsIndexControlRenderer(esDocDef).renderToDir(this.kotlinOutputDir)
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
