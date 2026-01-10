package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef

data class ModuleGeneratorFixture(
    val maiaGenerationContext: MaiaGenerationContext,
    val modelDefs: List<ModelDef>,
) {


    companion object {


        fun from(args: Array<String>): ModuleGeneratorFixture {

            val moduleGeneratorArgs = ModuleGeneratorArgs(args)

            val maiaGenerationContext = MaiaGenerationContext(
                createTablesSqlScriptPrefix = moduleGeneratorArgs.createTablesSqlScriptPrefix,
                sqlCreateScriptsDir = moduleGeneratorArgs.sqlCreateScriptsDir,
                srcMainKotlinOutputDir = moduleGeneratorArgs.kotlinMainOutputDir,
                srcMainResourcesDir = moduleGeneratorArgs.resourcesMainOutputDir,
                srcTestKotlinOutputDir = moduleGeneratorArgs.kotlinTestOutputDir,
                srcTestResourcesDir = moduleGeneratorArgs.resourcesTestOutputDir,
                typescriptOutputDir = moduleGeneratorArgs.typescriptOutputDir,
            )

            val modelDefs = moduleGeneratorArgs.specificationClassNames.map { ModelDefInstantiator.instantiate(it) }

            return ModuleGeneratorFixture(maiaGenerationContext, modelDefs)

        }


    }


}
