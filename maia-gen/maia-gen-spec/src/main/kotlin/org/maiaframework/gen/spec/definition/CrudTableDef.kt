package org.maiaframework.gen.spec.definition

class CrudTableDef(val dtoHtmlTableDef: DtoHtmlTableDef, val entityCrudApiDef: EntityCrudApiDef) {

    val crudServiceClassName: String = entityCrudApiDef.entityDef.crudAngularComponentNames.serviceName

    val crudTableComponentSelector: String = "app-${dtoHtmlTableDef.dtoBaseName.toKebabCase()}-crud-table"

    val dtoTableComponent: AngularComponentNames = dtoHtmlTableDef.tableComponent

    val crudTableComponentClassName: String = "${dtoHtmlTableDef.dtoBaseName}CrudTableComponent"

    val crudTableComponentHtmlFileName: String = "${dtoHtmlTableDef.dtoBaseName.toKebabCase()}-crud-table.component.html"

    val searchableDtoImportStatement: String = dtoHtmlTableDef.dtoDef.typescriptDtoImportStatement

    val createDialogComponentImportStatement = entityCrudApiDef.createApiDef?.angularDialogComponentImportStatement

    val deleteDialogComponentImportStatement = entityCrudApiDef.deleteApiDef?.angularDialogComponentImportStatement

    val editDialogComponentImportStatement = entityCrudApiDef.updateApiDef?.angularDialogComponentNames?.componentImportStatement

    val tableComponentImportStatement: String = dtoHtmlTableDef.tableComponent.componentImportStatement

    val crudServiceImportStatement: String = entityCrudApiDef.entityDef.crudAngularComponentNames.serviceImportStatement

    val htmlRenderedFilePath: String = "app/gen-components/${dtoHtmlTableDef.packageName.asTypescriptDirs()}/${dtoHtmlTableDef.dtoBaseName.toKebabCase()}-crud-table.component.html"

    val componentRenderedFilePath: String = "app/gen-components/${dtoHtmlTableDef.packageName.asTypescriptDirs()}/${dtoHtmlTableDef.dtoBaseName.toKebabCase()}-crud-table.component.ts"

}
