package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EntityRepoRenderer
import org.maiaframework.gen.spec.definition.ModelDef


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
