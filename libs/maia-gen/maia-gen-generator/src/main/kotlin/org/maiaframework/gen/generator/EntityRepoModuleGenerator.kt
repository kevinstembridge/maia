package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EntityRepoRenderer
import org.maiaframework.gen.spec.definition.ModelDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = EntityRepoModuleGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource()

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class EntityRepoModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        `render Entity repos`()

    }


    private fun `render Entity repos`() {

        this.modelDef.entityHierarchies.forEach {
            EntityRepoRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


}
