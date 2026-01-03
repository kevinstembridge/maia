package org.maiaframework.gen.plugin

import org.gradle.workers.WorkAction
import org.maiaframework.gen.generator.ModelDefInstantiator
import org.maiaframework.gen.generator.ModelGeneratorContext
import org.maiaframework.gen.generator.ModuleGeneratorInstantiator

abstract class GenerateModelWorkAction : WorkAction<GenerateModelWorkParameters> {


    override fun execute() {

        val srcMainKotlinDir = parameters.srcMainKotlinDir.get().asFile
        val srcMainResourcesDir = parameters.srcMainResourcesDir.get().asFile
        val typescriptOutputDir = parameters.typescriptOutputDir.get().asFile
        val sqlCreateScriptDir = parameters.sqlCreateScriptDir.get().asFile
        val createTableSqlScriptRenderedFilePath = parameters.createTableSqlScriptRenderedFilePath.get()

        val modelGeneratorContext = ModelGeneratorContext(
            srcMainKotlinDir,
            srcMainResourcesDir,
            typescriptOutputDir,
            sqlCreateScriptDir,
            createTableSqlScriptRenderedFilePath
        )

        val specificationClassName = parameters.specificationClassName.get()

        val modelDef = ModelDefInstantiator.instantiate(specificationClassName)

        val moduleGeneratorClassName = parameters.moduleGeneratorClassName.get()

        val moduleGenerator = ModuleGeneratorInstantiator.instantiate(moduleGeneratorClassName, modelGeneratorContext)

        println("found modelDef: ${modelDef.appKey}")

        moduleGenerator.generateSource(modelDef)

    }


}
