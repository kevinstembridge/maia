package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ModelDefProvider


object ModelDefInstantiator {


    fun instantiate(specClassname: String): ModelDef {

        val clazz = Class.forName(specClassname)
        val constructor = clazz.constructors[0]
        val provider = constructor.newInstance() as ModelDefProvider
        return provider.modelDef

    }


}
