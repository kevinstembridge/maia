package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.lang.text.StringFunctions.firstToUpper

class AngularComponentNames(packageName: PackageName, val baseName: String) {

    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(packageName)
    private val baseNameKebab = StringFunctions.toKebabCase(baseName)
    val componentName = "${baseName}Component"
    val agGridDatasourceClassName = "${baseName}AgGridDatasource"
    val agGridDatasourceImportStatement = "import {$agGridDatasourceClassName} from '@$genComponentsBaseDir/${agGridDatasourceClassName}';"
    val agGridDatasourceRenderedFilePath = "$genComponentsBaseDir/${agGridDatasourceClassName}.ts"
    val htmlFileName = "${baseNameKebab}.component.html"
    val componentScssFileName = "${baseNameKebab}.component.scss"
    val componentSelector = "app-$baseNameKebab"
    val componentImportStatement = "import {$componentName} from '@$genComponentsBaseDir/${baseNameKebab}.component';"
    val apiServiceName = "${baseName}ApiService"
    val apiServiceTypescriptImport = TypescriptImport(apiServiceName, "@$genComponentsBaseDir/${baseNameKebab}-api.service")
    val apiServiceRenderedFilePath = "$genComponentsBaseDir/${baseNameKebab}-api.service.ts"
    val serviceName = "${baseName}Service"
    val notifierName = "${baseName}Notifier"
    val serviceImportStatement = "import {$serviceName} from '@$genComponentsBaseDir/${baseNameKebab}.service';"
    val serviceTypescriptImport = TypescriptImport(serviceName, "@$genComponentsBaseDir/${baseNameKebab}.service")
    val componentRenderedFilePath = "$genComponentsBaseDir/${baseNameKebab}.component.ts"
    val serviceRenderedFilePath = "$genComponentsBaseDir/${baseNameKebab}.service.ts"
    val componentScssRenderedFilePath = "$genComponentsBaseDir/$componentScssFileName"
    val htmlRenderedFilePath = "$genComponentsBaseDir/$htmlFileName"
    val htmlFormName = HtmlFormName(firstToUpper(baseName))

}
