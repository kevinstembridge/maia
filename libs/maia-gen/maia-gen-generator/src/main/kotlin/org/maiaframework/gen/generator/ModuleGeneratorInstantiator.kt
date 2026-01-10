package org.maiaframework.gen.generator


object ModuleGeneratorInstantiator {


    fun instantiate(
        specClassname: String,
        maiaGenerationContext: MaiaGenerationContext
    ): AbstractModuleGenerator {

        val clazz = Class.forName(specClassname)
        val constructor = clazz.constructors[0]
        val provider = constructor.newInstance(maiaGenerationContext) as AbstractModuleGenerator
        return provider

    }


}
