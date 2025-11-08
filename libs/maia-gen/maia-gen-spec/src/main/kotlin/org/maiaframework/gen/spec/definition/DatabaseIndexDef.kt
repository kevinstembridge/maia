package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.lang.text.StringFunctions

class DatabaseIndexDef(
    val indexDef: IndexDef,
    val packageName: PackageName,
    baseName: DatabaseIndexBaseName,
    val moduleName: ModuleName?,
    val apiServiceName: String
) {


    val isNotIdAndVersionIndex = indexDef.isForIdAndVersion == false


    val isUnique = indexDef.isUnique


    val fieldNamesAnded = indexDef.entityFieldDefs.joinToString("And") { it.classFieldName.firstToUpper() }


    val existsByFunctionName = "existsBy$fieldNamesAnded"


    val existsByFunctionNameSnakeCase = StringFunctions.toSnakeCase(existsByFunctionName)


    private val modulePath = this.moduleName?.let { "${it.value}/" } ?: ""


    val existsByUrl = "/api/$modulePath${baseName.toSnakeCase()}/$existsByFunctionNameSnakeCase"


    val asyncValidator = AsyncValidatorDef(this.packageName, "${baseName.firstToUpper()}$fieldNamesAnded")


    val validatorName = asyncValidator.asyncValidatorName


    val validatorFieldName = asyncValidator.validatorFieldName


    val withExistsEndpoint = indexDef.withExistsEndpoint


    val isMultiField = indexDef.isMultiField


}
