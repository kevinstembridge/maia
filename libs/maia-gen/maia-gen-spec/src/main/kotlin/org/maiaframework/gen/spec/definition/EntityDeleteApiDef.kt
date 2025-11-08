package org.maiaframework.gen.spec.definition

class EntityDeleteApiDef(
        val entityDef: EntityDef,
        val crudApiDef: CrudApiDef,
        private val moduleName: ModuleName?
) {

    val preAuthorizeExpression = this.crudApiDef.authority?.let { PreAuthorizeExpression("hasAuthority('$it')") }

    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"
    val endpointUrl = "/api/${modulePath}${this.entityDef.entityBaseName.toSnakeCase()}/"

    private val angularComponentNames = AngularComponentNames(this.entityDef.packageName, "${entityDef.entityBaseName}DeleteDialog")

    val angularDialogComponentName = angularComponentNames.componentName
    val angularDialogComponentHtmlFileName = angularComponentNames.htmlFileName
    val angularDialogComponentSelector = angularComponentNames.componentSelector
    val dialogComponentRenderedFilePath = angularComponentNames.componentRenderedFilePath
    val dialogHtmlRenderedFilePath = angularComponentNames.htmlRenderedFilePath
    val angularDialogComponentImportStatement = angularComponentNames.componentImportStatement


}
