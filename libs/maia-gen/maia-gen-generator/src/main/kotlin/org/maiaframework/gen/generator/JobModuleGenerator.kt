package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.RefreshEsIndexJobRenderer
import org.maiaframework.gen.spec.definition.EsDocDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = JobModuleGenerator(moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class JobModuleGenerator(
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        renderTypeaheadIndexRefreshJobs()
        renderEsIndexRefreshJobs()

    }


    private fun renderTypeaheadIndexRefreshJobs() {

        renderEsIndexRefreshJobs(this.modelDef.typeaheadDefs.map { it.esDocDef })

    }


    private fun renderEsIndexRefreshJobs() {

        renderEsIndexRefreshJobs(this.modelDef.esDocsDefs)

    }


    private fun renderEsIndexRefreshJobs(esDocDefs: List<EsDocDef>) {

        esDocDefs.filter { it.generateRefreshIndexJob }.forEach { esDocDef ->
            val renderer = RefreshEsIndexJobRenderer(esDocDef)
            renderer.renderToDir(this.kotlinOutputDir)
        }

    }


}
