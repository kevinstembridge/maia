package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName

object GeneratedTypescriptDir {


    fun forPackage(packageName: PackageName): String {

        return "app/gen-components/${packageName.asTypescriptDirs()}"

    }


}
