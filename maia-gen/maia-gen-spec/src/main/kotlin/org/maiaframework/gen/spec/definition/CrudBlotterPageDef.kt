package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class CrudBlotterPageDef(
    val blotterDef: BlotterDef,
    val pageTitle: String,
) {


    private val genDir = GeneratedTypescriptDir.forPackage(blotterDef.packageName)


    val dataPageId = "${blotterDef.dtoBaseName.toSnakeCase()}_blotter"


    val pageAngularComponentNames = AngularComponentNames(
        blotterDef.packageName,
        "${blotterDef.dtoBaseName}BlotterPage"
    )


    val crudBlotterSelector = "app-${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter"


    val crudBlotterComponentClassName = "${blotterDef.dtoBaseName}CrudBlotterComponent"


    val crudBlotterComponentTypescriptImport = TypescriptImport(
        name = crudBlotterComponentClassName,
        from = "@$genDir/${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter"
    )


}
