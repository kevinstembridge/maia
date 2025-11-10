package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.lang.ConstructorArg


class EntityPkRenderer(
    entityDef: EntityDef
) : AbstractKotlinRenderer(
    entityDef.entityPkClassDef
) {


    init {

        val constructorArgs = this.classDef.allFields.map { ConstructorArg(it) }
        setConstructorArgs(constructorArgs)

    }


}
