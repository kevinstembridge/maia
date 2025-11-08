package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocMapperRenderer
import org.maiaframework.gen.renderers.EsDocTableDtoMapperRenderer
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.ModelDef


class EsDocsModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        val allEsDocs = this.modelDef.allEsDocDefs

        allEsDocs.forEach {
            renderEsDocMapper(it)
        }

        renderEsDocMappersForTableDtos()

    }


    private fun renderEsDocMapper(esDocDef: EsDocDef) {

        EsDocMapperRenderer(esDocDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderEsDocMappersForTableDtos() {

        this.modelDef.dtoHtmlTableDefs.filter { it.dtoHtmlTableSourceDef.esDocDef != null }.forEach {

            EsDocTableDtoMapperRenderer(it).renderToDir(this.kotlinOutputDir)

        }

    }


}
