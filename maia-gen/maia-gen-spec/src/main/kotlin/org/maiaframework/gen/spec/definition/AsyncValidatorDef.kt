package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.lang.Uqcn
import org.maiaframework.lang.text.StringFunctions

class AsyncValidatorDef(
    packageName: PackageName,
    baseName: String,
    /** The name of the API service function that checks whether a value already exists. */
    val existsByFunctionName: String,
    /** The class name of the service, which may belong to a different form, that has [existsByFunctionName]. */
    val apiServiceName: String,
    val apiServiceTypescriptImport: TypescriptImport,
    /** The names of the request DTO fields, which are the entity fields of the unique index being checked. */
    val requestFieldNames: List<String>
) {


    val apiServiceFieldName = StringFunctions.firstToLower(apiServiceName)


    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(packageName)


    val asyncValidatorName = "${baseName}AsyncValidator"


    val validatorFieldName = StringFunctions.firstToLower(asyncValidatorName)


    val asyncValidatorFileName = "${asyncValidatorName}.ts"


    val asyncValidationDtoBaseName = DtoBaseName(baseName)


    val asyncValidationDtoSuffix = DtoSuffix("RequestDto")


    val asyncValidationDtoName = asyncValidationDtoBaseName.withSuffix(asyncValidationDtoSuffix)


    val asyncValidationDtoUqcn = Uqcn(asyncValidationDtoName.value)


    val asyncValidatorTypescriptImport = TypescriptImport(asyncValidatorName, "@$genComponentsBaseDir/${asyncValidatorName}")


    val asyncValidationDtoTypescriptImport = TypescriptImport(asyncValidationDtoName.value, "@$genComponentsBaseDir/$asyncValidationDtoName")


    val asyncValidationDtoImportStatement = "import {$asyncValidationDtoName} from '@$genComponentsBaseDir/${asyncValidationDtoName}';"


    val asyncValidatorRenderedFilePath = "$genComponentsBaseDir/${asyncValidatorFileName}"


    val asyncValidationDtoRenderedFilePath = "$genComponentsBaseDir/${asyncValidationDtoName}.ts"


}
