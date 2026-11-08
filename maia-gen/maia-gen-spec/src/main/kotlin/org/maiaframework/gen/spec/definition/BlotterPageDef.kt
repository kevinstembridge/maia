package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class BlotterPageDef(
    val blotterDef: BlotterDef,
    val pageTitle: String,
) {


    private val genDir = GeneratedTypescriptDir.forPackage(blotterDef.packageName)


    val dataPageId = "${blotterDef.dtoBaseName.toSnakeCase()}_blotter"


    val pageAngularComponentNames = AngularComponentNames(
        blotterDef.packageName,
        "${blotterDef.dtoBaseName}BlotterPage"
    )


    val blotterComponentSelector = "app-${blotterDef.dtoBaseName.toKebabCase()}-blotter${blotterDef.versionSuffixKebab}"


    private val modulePath = if (blotterDef.moduleName == null) "" else "${blotterDef.moduleName.value}/"


    val routePath = "$modulePath${blotterDef.dtoBaseName.toKebabCase()}-blotter"


    val blotterComponentClassName = "${blotterDef.dtoBaseName}Blotter${blotterDef.versionSuffix}"


    val blotterComponentTypescriptImport = TypescriptImport(
        name = blotterComponentClassName,
        from = "@$genDir/${blotterDef.dtoBaseName.toKebabCase()}-blotter${blotterDef.versionSuffixKebab}"
    )


}
