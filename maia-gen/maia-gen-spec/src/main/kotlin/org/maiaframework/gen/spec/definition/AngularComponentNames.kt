package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.lang.text.StringFunctions.firstToUpper

class AngularComponentNames(packageName: PackageName, val baseName: String) {

    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(packageName)
    val componentName = baseName
    val componentNameKebab = StringFunctions.toKebabCase(componentName)
    val agGridDatasourceClassName = "${baseName}AgGridDatasource"
    val agGridDatasourceImportStatement = "import {$agGridDatasourceClassName} from '@$genComponentsBaseDir/${agGridDatasourceClassName}';"
    val agGridDatasourceTypescriptImport = TypescriptImport(agGridDatasourceClassName, "@$genComponentsBaseDir/$agGridDatasourceClassName")
    val agGridDatasourceRenderedFilePath = "$genComponentsBaseDir/${agGridDatasourceClassName}.ts"
    val htmlFileName = "${componentNameKebab}.html"
    val componentScssFileName = "${componentNameKebab}.scss"
    val componentSelector = "app-$componentNameKebab"
    val componentImportStatement = "import {$componentName} from '@$genComponentsBaseDir/${componentNameKebab}';"
    val componentTypescriptImport = TypescriptImport(componentName, "@$genComponentsBaseDir/${componentNameKebab}")
    val apiServiceName = "${baseName}ApiService"
    val apiServiceTypescriptImport = TypescriptImport(apiServiceName, "@$genComponentsBaseDir/${componentNameKebab}-api.service")
    val apiServiceRenderedFilePath = "$genComponentsBaseDir/${componentNameKebab}-api.service.ts"
    val serviceName = "${baseName}Service"
    val notifierName = "${baseName}Notifier"
    val serviceImportStatement = "import {$serviceName} from '@$genComponentsBaseDir/${componentNameKebab}.service';"
    val serviceTypescriptImport = TypescriptImport(serviceName, "@$genComponentsBaseDir/${componentNameKebab}.service")
    val componentRenderedFilePath = "$genComponentsBaseDir/${componentNameKebab}.ts"
    val serviceRenderedFilePath = "$genComponentsBaseDir/${componentNameKebab}.service.ts"
    val componentScssRenderedFilePath = "$genComponentsBaseDir/$componentScssFileName"
    val htmlRenderedFilePath = "$genComponentsBaseDir/$htmlFileName"
    val htmlFormName = HtmlFormName(firstToUpper(baseName))

}
