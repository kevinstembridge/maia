package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.lang.Uqcn

data class EnumOptionsDef(val fqcn: Fqcn) {
    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(fqcn.packageName)
    val uqcn: Uqcn = fqcn.uqcn
    val selectOptionsUqcn = uqcn.withSuffix("SelectOptions")
    val selectOptionsTypescriptImport = TypescriptImport(
        selectOptionsUqcn.value,
        "@$genComponentsBaseDir/$selectOptionsUqcn"
    )
}
