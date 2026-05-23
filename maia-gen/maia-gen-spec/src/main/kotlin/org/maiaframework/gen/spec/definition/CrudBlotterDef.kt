package org.maiaframework.gen.spec.definition

class CrudBlotterDef(
    val blotterDef: BlotterDef,
    val entityCrudApiDef: EntityCrudApiDef
) {


    val crudBlotterComponentSelector: String = "app-${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter"


    val dtoBlotterComponent: AngularComponentNames = blotterDef.blotterComponent


    val crudBlotterComponentClassName: String = "${blotterDef.dtoBaseName}CrudBlotterComponent"


    val crudBlotterComponentHtmlFileName: String = "${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter.html"


    val searchableDtoImportStatement: String = blotterDef.dtoDef.typescriptDtoImportStatement


    val blotterComponentImportStatement: String = blotterDef.blotterComponent.componentImportStatement


    val htmlRenderedFilePath: String = "app/gen-components/${blotterDef.packageName.asTypescriptDirs()}/${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter.html"


    val componentRenderedFilePath: String = "app/gen-components/${blotterDef.packageName.asTypescriptDirs()}/${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter.ts"


}
