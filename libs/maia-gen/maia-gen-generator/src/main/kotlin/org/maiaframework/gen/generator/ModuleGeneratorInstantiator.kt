package org.maiaframework.gen.generator


object ModuleGeneratorInstantiator {


    fun instantiate(
        specClassname: String,
        modelGeneratorContext: ModelGeneratorContext
    ): AbstractModuleGenerator {

        val clazz = Class.forName(specClassname)
        val constructor = clazz.constructors[0]
        val provider = constructor.newInstance(modelGeneratorContext) as AbstractModuleGenerator
        return provider

    }


}
