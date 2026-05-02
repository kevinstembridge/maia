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
    val apiServiceName = "${componentName}ApiService"
    val apiServiceNameKebab = StringFunctions.toKebabCase(apiServiceName)
    val apiServiceTypescriptImport = TypescriptImport(apiServiceName, "@$genComponentsBaseDir/$apiServiceNameKebab")
    val apiServiceRenderedFilePath = "$genComponentsBaseDir/$apiServiceNameKebab.ts"
    val serviceName = "${baseName}Service"
    val serviceNameKebab = StringFunctions.toKebabCase(serviceName)
    val notifierName = "${baseName}Notifier"
    val serviceImportStatement = "import {$serviceName} from '@$genComponentsBaseDir/${serviceNameKebab}';"
    val serviceTypescriptImport = TypescriptImport(serviceName, "@$genComponentsBaseDir/${serviceNameKebab}")
    val componentRenderedFilePath = "$genComponentsBaseDir/${componentNameKebab}.ts"
    val serviceRenderedFilePath = "$genComponentsBaseDir/${serviceNameKebab}.ts"
    val componentScssRenderedFilePath = "$genComponentsBaseDir/$componentScssFileName"
    val htmlRenderedFilePath = "$genComponentsBaseDir/$htmlFileName"
    val htmlFormName = HtmlFormName(firstToUpper(baseName))

}
