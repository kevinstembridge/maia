package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef

data class ModuleGeneratorFixture(
    val modelGeneratorContext: ModelGeneratorContext,
    val modelDefs: List<ModelDef>,
) {


    companion object {


        fun from(args: Array<String>): ModuleGeneratorFixture {

            val modelGeneratorArgs = ModelGeneratorArgs(args)
            val modelGeneratorContext = ModelGeneratorContext(
                modelGeneratorArgs.kotlinOutputDir,
                modelGeneratorArgs.resourcesOutputDir,
                modelGeneratorArgs.typescriptOutputDir,
                modelGeneratorArgs.sqlCreateScriptsDir,
                modelGeneratorArgs.createTablesSqlScriptPrefix,
            )

            val modelDefs = modelGeneratorArgs.specificationClassNames.map { ModelDefInstantiator.instantiate(it) }

            return ModuleGeneratorFixture(modelGeneratorContext, modelDefs)

        }


    }


}
