package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.lang.text.StringFunctions.firstToUpper
import org.maiaframework.lang.text.StringFunctions.toKebabCase

/**
 * @param versionSuffix trailing version suffix such as `_v0002` (see [VersionSuffix]), or empty. It is appended last to
 * every derived name, so that the version is always the very end of a class name and of a file name.
 */
class AngularComponentNames(packageName: PackageName, val baseName: String, versionSuffix: String = "") {

    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(packageName)
    val componentName = "$baseName$versionSuffix"
    val componentNameKebab = toKebabCase(componentName)
    val agGridDatasourceClassName = "${baseName}AgGridDatasource$versionSuffix"
    val agGridDatasourceImportStatement = "import {$agGridDatasourceClassName} from '@$genComponentsBaseDir/${agGridDatasourceClassName}';"
    val agGridDatasourceTypescriptImport = TypescriptImport(agGridDatasourceClassName, "@$genComponentsBaseDir/$agGridDatasourceClassName")
    val agGridDatasourceRenderedFilePath = "$genComponentsBaseDir/${agGridDatasourceClassName}.ts"
    val htmlFileName = "${componentNameKebab}.html"
    val componentScssFileName = "${componentNameKebab}.scss"
    val componentSelector = "app-$componentNameKebab"
    val componentImportStatement = "import {$componentName} from '@$genComponentsBaseDir/${componentNameKebab}';"
    val componentTypescriptImport = TypescriptImport(componentName, "@$genComponentsBaseDir/${componentNameKebab}")
    val apiServiceName = "${baseName}ApiService$versionSuffix"
    val apiServiceNameKebab = toKebabCase(apiServiceName)
    val apiServiceTypescriptImport = TypescriptImport(apiServiceName, "@$genComponentsBaseDir/$apiServiceNameKebab")
    val apiServiceRenderedFilePath = "$genComponentsBaseDir/$apiServiceNameKebab.ts"
    val serviceName = "${baseName}Service$versionSuffix"
    val serviceNameKebab = toKebabCase(serviceName)
    val notifierName = "${baseName}Notifier$versionSuffix"
    val serviceImportStatement = "import {$serviceName} from '@$genComponentsBaseDir/${serviceNameKebab}';"
    val serviceTypescriptImport = TypescriptImport(serviceName, "@$genComponentsBaseDir/${serviceNameKebab}")
    val componentRenderedFilePath = "$genComponentsBaseDir/${componentNameKebab}.ts"
    val serviceRenderedFilePath = "$genComponentsBaseDir/${serviceNameKebab}.ts"
    val componentScssRenderedFilePath = "$genComponentsBaseDir/$componentScssFileName"
    val htmlRenderedFilePath = "$genComponentsBaseDir/$htmlFileName"
    val htmlFormName = HtmlFormName(firstToUpper(componentName))

}
