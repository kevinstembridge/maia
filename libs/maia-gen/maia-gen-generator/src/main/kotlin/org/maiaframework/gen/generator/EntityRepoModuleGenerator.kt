package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EntityRepoRenderer


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = EntityRepoModuleGenerator(moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class EntityRepoModuleGenerator(
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
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
