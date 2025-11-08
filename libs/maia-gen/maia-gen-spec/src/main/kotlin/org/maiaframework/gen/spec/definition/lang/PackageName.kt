package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.types.StringType

class PackageName(value: String) : StringType<PackageName>(value) {


    fun plusSubPackage(subPackage: String): PackageName {

        return PackageName("$value.$subPackage")

    }


    fun plusSubPackage(subPackage: PackageName): PackageName {

        return PackageName("$value.$subPackage")

    }


    fun uqcn(uqcn: String): Fqcn {

        return uqcn(Uqcn(uqcn))

    }


    fun uqcn(uqcn: Uqcn): Fqcn {

        return Fqcn.valueOf(this, uqcn)

    }


    fun asTypescriptDirs(): String {

        return StringFunctions.toKebabCase(this.value.replace(".", "/"))

    }


}
