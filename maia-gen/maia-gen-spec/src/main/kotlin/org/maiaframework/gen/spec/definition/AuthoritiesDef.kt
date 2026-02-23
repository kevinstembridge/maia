package org.maiaframework.gen.spec.definition


data class AuthoritiesDef(val enumDef: EnumDef) {


    val importStatement = "import { ${enumDef.uqcn} } from '@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/${enumDef.uqcn}';"


    val authGuardRenderedFilePath = "${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/${enumDef.uqcn}.ts"


}
