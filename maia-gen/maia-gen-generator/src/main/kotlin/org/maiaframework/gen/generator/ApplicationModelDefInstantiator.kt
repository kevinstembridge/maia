package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.ApplicationModelDefProvider
import org.maiaframework.gen.spec.definition.ApplicationModelDef


object ApplicationModelDefInstantiator {


    fun instantiate(specClassname: String): ApplicationModelDef {

        val clazz = Class.forName(specClassname)
        val constructor = clazz.constructors[0]
        val provider = constructor.newInstance() as ApplicationModelDefProvider
        return provider.applicationModelDef

    }


}
