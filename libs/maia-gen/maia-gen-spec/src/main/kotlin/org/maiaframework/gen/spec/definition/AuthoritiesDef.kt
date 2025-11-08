package org.maiaframework.gen.spec.definition

data class AuthoritiesDef(val enumDef: EnumDef) {


    val importStatement = "import { ${enumDef.fqcn.uqcn} } from '@${GeneratedTypescriptDir.forPackage(enumDef.fqcn.packageName)}/${enumDef.fqcn.uqcn}';"

}
