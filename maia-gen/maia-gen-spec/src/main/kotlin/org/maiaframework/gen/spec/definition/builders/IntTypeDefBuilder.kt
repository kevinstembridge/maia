package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.Uqcn

class IntTypeDefBuilder {


    private val fqcn: Fqcn
    private var provided: Boolean = false


    constructor(packageName: String, typeName: String) {

        BlankStringException.throwIfBlank(typeName, "typeName")

        this.fqcn = Fqcn.valueOf(PackageName(packageName), Uqcn(typeName))

    }


    constructor(rawFqcn: String) {

        BlankStringException.throwIfBlank(rawFqcn, "rawFqcn")

        this.fqcn = Fqcn.valueOf(rawFqcn)

    }


    fun provided(): IntTypeDefBuilder {

        this.provided = true
        return this

    }


    fun build(): IntTypeDef {

        return IntTypeDef(this.fqcn, this.provided)

    }


}
