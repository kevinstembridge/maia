package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException
import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.Uqcn


@MaiaDslMarker
class BooleanTypeDefBuilder {


    private val fqcn: Fqcn
    private var provided: Boolean = false


    constructor(packageName: String, typeName: String) {

        BlankStringException.throwIfBlank(packageName, "packageName")
        BlankStringException.throwIfBlank(typeName, "typeName")

        this.fqcn = Fqcn.valueOf(PackageName(packageName), Uqcn(typeName))

    }


    constructor(rawFqcn: String) {

        BlankStringException.throwIfBlank(rawFqcn, "rawFqcn")

        this.fqcn = Fqcn.valueOf(rawFqcn)

    }


    fun provided(): BooleanTypeDefBuilder {

        this.provided = true
        return this

    }


    fun build(): BooleanTypeDef {

        return BooleanTypeDef(this.fqcn, this.provided)

    }


}
