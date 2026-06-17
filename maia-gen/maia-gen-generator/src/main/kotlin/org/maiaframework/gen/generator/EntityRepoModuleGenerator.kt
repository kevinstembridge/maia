package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EntityRepoRenderer


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = EntityRepoModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class EntityRepoModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
) : AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        `render Entity repos`()

    }


    private fun `render Entity repos`() {

        this.applicationModelDef.entityHierarchies.forEach {
            EntityRepoRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


}
