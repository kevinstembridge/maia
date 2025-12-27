package org.maiaframework.gen.plugin

import org.gradle.workers.WorkAction
import org.maiaframework.gen.generator.ModelDefInstantiator

abstract class GenerateModelWorkAction : WorkAction<GenerateModelWorkParameters> {


    override fun execute() {

        val specificationClassName = parameters.specificationClassName.get()

        val modelDef = ModelDefInstantiator.instantiate(specificationClassName)

         println("found modelDef: ${modelDef.appKey}")

    }


}
