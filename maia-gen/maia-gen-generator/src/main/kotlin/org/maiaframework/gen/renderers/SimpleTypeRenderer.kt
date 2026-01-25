package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.SimpleTypeDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


class SimpleTypeRenderer(simpleTypeDef: SimpleTypeDef) : AbstractKotlinRenderer(simpleTypeDef.classDef) {


    init {


        addConstructorArg(ClassFieldDef.aClassField("value", simpleTypeDef.simpleTypeUnderlyingFieldType).build())

    }


    override fun renderFunctions() {

        // do nothing

    }


}
