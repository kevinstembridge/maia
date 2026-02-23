package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.TypescriptImport


data class AuthoritiesDef(val enumDef: EnumDef) {


    val importStatement = "import { ${enumDef.uqcn} } from '@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/${enumDef.uqcn}';"


    val authGuardRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/auth.guard.ts"


    val authServiceRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/auth.service.ts"


    val authApiServiceRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/auth-api.service.ts"


    val userSummaryDtoRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/UserSummaryDto.ts"


    val userSummaryDtoTypescriptImport = TypescriptImport(name = "UserSummaryResponseDto", "@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}")


    val authApiServiceTypescriptImport = TypescriptImport(name = "AuthApiService", "@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}")


}
