package org.maiaframework.gen.spec.definition

class CrudBlotterDef(val blotterDef: BlotterDef, val entityCrudApiDef: EntityCrudApiDef) {


    val crudServiceClassName: String = entityCrudApiDef.entityDef.crudAngularComponentNames.serviceName


    val crudBlotterComponentSelector: String = "app-${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter"


    val dtoBlotterComponent: AngularComponentNames = blotterDef.blotterComponent


    val crudBlotterComponentClassName: String = "${blotterDef.dtoBaseName}CrudBlotterComponent"


    val crudBlotterComponentHtmlFileName: String = "${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter.component.html"


    val searchableDtoImportStatement: String = blotterDef.dtoDef.typescriptDtoImportStatement


    val createDialogComponentImportStatement = entityCrudApiDef.createApiDef?.angularDialogComponentImportStatement


    val deleteDialogComponentImportStatement = entityCrudApiDef.deleteApiDef?.angularDialogComponentImportStatement


    val editDialogComponentImportStatement = entityCrudApiDef.updateApiDef?.angularDialogComponentNames?.componentImportStatement


    val blotterComponentImportStatement: String = blotterDef.blotterComponent.componentImportStatement


    val crudServiceImportStatement: String = entityCrudApiDef.entityDef.crudAngularComponentNames.serviceImportStatement


    val htmlRenderedFilePath: String = "app/gen-components/${blotterDef.packageName.asTypescriptDirs()}/${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter.component.html"


    val componentRenderedFilePath: String = "app/gen-components/${blotterDef.packageName.asTypescriptDirs()}/${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter.component.ts"


}
