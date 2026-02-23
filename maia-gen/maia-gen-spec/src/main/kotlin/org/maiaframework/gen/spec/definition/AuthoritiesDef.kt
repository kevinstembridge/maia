package org.maiaframework.gen.spec.definition


data class AuthoritiesDef(val enumDef: EnumDef) {


    val importStatement = "import { ${enumDef.uqcn} } from '@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/${enumDef.uqcn}';"


    val authGuardRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/auth.guard.ts"


    val authServiceRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/auth.service.ts"


    val authApiServiceRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/auth-api.service.ts"


    val userSummaryDtoRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/UserSummaryDto.ts"


}
