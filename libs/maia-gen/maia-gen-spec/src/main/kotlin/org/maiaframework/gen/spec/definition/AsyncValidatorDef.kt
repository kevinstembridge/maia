package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.lang.Uqcn
import org.maiaframework.lang.text.StringFunctions

class AsyncValidatorDef(packageName: PackageName, baseName: String) {


    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(packageName)


    val asyncValidatorName = "${baseName}AsyncValidator"


    val validatorFieldName = StringFunctions.firstToLower(asyncValidatorName)


    val asyncValidatorFileName = "${asyncValidatorName}.ts"


    val asyncValidationDtoBaseName = DtoBaseName(baseName)


    val asyncValidationDtoSuffix = DtoSuffix("RequestDto")


    val asyncValidationDtoName = asyncValidationDtoBaseName.withSuffix(asyncValidationDtoSuffix)


    val asyncValidationDtoUqcn = Uqcn(asyncValidationDtoName.value)


    val asyncValidatorTypescriptImport = TypescriptImport(asyncValidatorName, "@$genComponentsBaseDir/${asyncValidatorName}")


    val asyncValidationDtoImportStatement = "import {$asyncValidationDtoName} from '@$genComponentsBaseDir/${asyncValidationDtoName}';"


    val asyncValidatorRenderedFilePath = "$genComponentsBaseDir/${asyncValidatorFileName}"


    val asyncValidationDtoRenderedFilePath = "$genComponentsBaseDir/${asyncValidationDtoName}.ts"


}
