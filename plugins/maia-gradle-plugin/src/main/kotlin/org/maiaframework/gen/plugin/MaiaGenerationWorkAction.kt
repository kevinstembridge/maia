package org.maiaframework.gen.plugin

import org.gradle.workers.WorkAction
import org.maiaframework.gen.generator.ModelDefInstantiator
import org.maiaframework.gen.generator.MaiaGenerationContext
import org.maiaframework.gen.generator.ModuleGeneratorInstantiator

abstract class MaiaGenerationWorkAction : WorkAction<MaiaGenerationWorkParameters> {


    override fun execute() {

        val maiaGenerationContext = MaiaGenerationContext(
            createTablesSqlScriptPrefix = this.parameters.createTableSqlScriptPrefix.get(),
            sqlCreateScriptsDir = this.parameters.sqlCreateScriptDir.get().asFile,
            srcMainKotlinOutputDir = this.parameters.srcMainKotlinDir.get().asFile,
            srcMainResourcesDir = this.parameters.srcMainResourcesDir.get().asFile,
            srcTestKotlinOutputDir = this.parameters.srcTestKotlinDir.get().asFile,
            srcTestResourcesDir = this.parameters.srcTestResourcesDir.get().asFile,
            typescriptOutputDir = this.parameters.typescriptOutputDir.get().asFile
        )

        val specificationClassName = parameters.specificationClassName.get()

        val modelDef = ModelDefInstantiator.instantiate(specificationClassName)

        val moduleGeneratorClassName = parameters.moduleGeneratorClassName.get()

        val moduleGenerator = ModuleGeneratorInstantiator.instantiate(moduleGeneratorClassName, maiaGenerationContext)

        moduleGenerator.generateSource(modelDef)

    }


}
