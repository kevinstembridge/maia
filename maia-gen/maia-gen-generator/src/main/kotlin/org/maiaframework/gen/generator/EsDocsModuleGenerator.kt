package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocMapperRenderer
import org.maiaframework.gen.renderers.EsDocBlotterRowDtoMapperRenderer
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.EsDocDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = EsDocsModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class EsDocsModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
) : AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        val allEsDocs = this.applicationModelDef.allEsDocDefs

        allEsDocs.forEach {
            renderEsDocMapper(it)
        }

        renderEsDocMappersForTableDtos()

    }


    private fun renderEsDocMapper(esDocDef: EsDocDef) {

        EsDocMapperRenderer(esDocDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderEsDocMappersForTableDtos() {

        this.applicationModelDef.blotterDefs
            .filter { it.blotterSourceDef is BlotterEsDocSourceDef }
            .forEach { EsDocBlotterRowDtoMapperRenderer(it).renderToDir(this.kotlinOutputDir) }

    }


}
