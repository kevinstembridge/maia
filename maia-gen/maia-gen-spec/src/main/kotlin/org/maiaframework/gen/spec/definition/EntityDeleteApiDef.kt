package org.maiaframework.gen.spec.definition

import org.maiaframework.lang.text.StringFunctions

class EntityDeleteApiDef(
    val entityDef: EntityDef,
    val crudApiDef: CrudApiDef,
    moduleName: ModuleName?
) {


    val preAuthorizeExpression = this.crudApiDef.authorityDef?.let { PreAuthorizeExpression("hasAuthority('$it')") }


    private val modulePath = if (moduleName == null) "" else "${StringFunctions.toKebabCase(moduleName.value)}/"


    val endpointUrl = "/api/${modulePath}${this.entityDef.entityBaseName.toKebabCase()}/"


    private val angularComponentNames =
        AngularComponentNames(this.entityDef.packageName, "${entityDef.entityBaseName}DeleteDialog")


    val angularDialogComponentName = angularComponentNames.componentName


    val angularDialogComponentHtmlFileName = angularComponentNames.htmlFileName


    val angularDialogComponentSelector = angularComponentNames.componentSelector


    val dialogComponentRenderedFilePath = angularComponentNames.componentRenderedFilePath


    val dialogHtmlRenderedFilePath = angularComponentNames.htmlRenderedFilePath


    val angularDialogComponentImportStatement = angularComponentNames.componentImportStatement


    val angularDialogComponentTypescriptImport = angularComponentNames.componentTypescriptImport


}
