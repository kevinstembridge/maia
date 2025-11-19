package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.RefreshEsIndexJobRenderer
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.ModelDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = JobModuleGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource()

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class JobModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelDef,
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
