package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.Uqcn


@MaiaDslMarker
class StringTypeDefBuilder {


    private val fqcn: Fqcn
    private var provided: Boolean = false
    private var caseMode: StringTypeDef.CaseMode = StringTypeDef.CaseMode.AS_PROVIDED


    constructor(packageName: String, typeName: String) {

        BlankStringException.throwIfBlank(packageName, "packageName")
        BlankStringException.throwIfBlank(typeName, "typeName")

        this.fqcn = Fqcn.valueOf(PackageName(packageName), Uqcn(typeName))

    }


    constructor(rawFqcn: String) {

        BlankStringException.throwIfBlank(rawFqcn, "rawFqcn")

        this.fqcn = Fqcn.valueOf(rawFqcn)

    }


    fun provided(): StringTypeDefBuilder {

        this.provided = true
        return this

    }


    fun alwaysUpperCase(): StringTypeDefBuilder {

        this.caseMode = StringTypeDef.CaseMode.ALWAYS_UPPER
        return this

    }


    fun alwaysLowerCase(): StringTypeDefBuilder {

        this.caseMode = StringTypeDef.CaseMode.ALWAYS_LOWER
        return this

    }


    fun build(): StringTypeDef {

        return StringTypeDef(this.fqcn, this.provided, this.caseMode)

    }


}
