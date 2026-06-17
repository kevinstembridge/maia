package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.RefreshEsIndexJobRenderer
import org.maiaframework.gen.spec.definition.EsDocDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = JobModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class JobModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
) : AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        renderTypeaheadIndexRefreshJobs()
        renderEsIndexRefreshJobs()

    }


    private fun renderTypeaheadIndexRefreshJobs() {

        renderEsIndexRefreshJobs(this.applicationModelDef.typeaheadDefs.map { it.esDocDef })

    }


    private fun renderEsIndexRefreshJobs() {

        renderEsIndexRefreshJobs(this.applicationModelDef.esDocsDefs)

    }


    private fun renderEsIndexRefreshJobs(esDocDefs: List<EsDocDef>) {

        esDocDefs
            .filter { it.generateRefreshIndexJob }
            .forEach { esDocDef -> RefreshEsIndexJobRenderer(esDocDef).renderToDir(this.kotlinOutputDir) }

    }


}
