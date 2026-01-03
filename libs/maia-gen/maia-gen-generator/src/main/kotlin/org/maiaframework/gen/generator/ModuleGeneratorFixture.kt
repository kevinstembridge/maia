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
                createTablesSqlScriptPrefix = modelGeneratorArgs.createTablesSqlScriptPrefix,
                sqlCreateScriptsDir = modelGeneratorArgs.sqlCreateScriptsDir,
                srcMainKotlinOutputDir = modelGeneratorArgs.kotlinMainOutputDir,
                srcMainResourcesDir = modelGeneratorArgs.resourcesMainOutputDir,
                srcTestKotlinOutputDir = modelGeneratorArgs.kotlinTestOutputDir,
                srcTestResourcesDir = modelGeneratorArgs.resourcesTestOutputDir,
                typescriptOutputDir = modelGeneratorArgs.typescriptOutputDir,
            )

            val modelDefs = modelGeneratorArgs.specificationClassNames.map { ModelDefInstantiator.instantiate(it) }

            return ModuleGeneratorFixture(modelGeneratorContext, modelDefs)

        }


    }


}
