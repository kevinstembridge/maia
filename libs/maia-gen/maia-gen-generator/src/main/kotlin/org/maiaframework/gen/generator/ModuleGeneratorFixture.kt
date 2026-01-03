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
                srcMainKotlinOutputDir = modelGeneratorArgs.kotlinMainOutputDir,
                srcTestKotlinOutputDir = modelGeneratorArgs.kotlinTestOutputDir,
                srcMainResourcesDir = modelGeneratorArgs.resourcesMainOutputDir,
                srcTestResourcesDir = modelGeneratorArgs.resourcesTestOutputDir,
                typescriptOutputDir = modelGeneratorArgs.typescriptOutputDir,
                sqlCreateScriptsDir = modelGeneratorArgs.sqlCreateScriptsDir,
                createTablesSqlScriptPrefix = modelGeneratorArgs.createTablesSqlScriptPrefix,
            )

            val modelDefs = modelGeneratorArgs.specificationClassNames.map { ModelDefInstantiator.instantiate(it) }

            return ModuleGeneratorFixture(modelGeneratorContext, modelDefs)

        }


    }


}
