package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.EnumType
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.lang.Uqcn


class EnumDef(
    val fqcn: Fqcn,
    val enumValueDefs: List<EnumValueDef>,
    val isProvided: Boolean,
    val withTypescript: Boolean,
    val withEnumSelectionOptions: Boolean
) {


    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(fqcn.packageName)


    val classDef: ClassDef = aClassDef(EnumType(fqcn)).ofType(ClassType.ENUM).build()


    val uqcn: Uqcn = this.classDef.uqcn


    val selectOptionsUqcn = uqcn.withSuffix("SelectOptions")


    val importStatement = "import {$uqcn} from '@$genComponentsBaseDir/${uqcn}';"


    val typescriptImport = TypescriptImport(uqcn.value, "@${GeneratedTypescriptDir.forPackage(fqcn.packageName)}/$uqcn")


    val selectOptionsTypescriptImport = TypescriptImport(selectOptionsUqcn.value, "@$genComponentsBaseDir/${selectOptionsUqcn}")


    val renderedTypescriptFilePath = "/app/gen-components/${this.fqcn.packageName.asTypescriptDirs()}/${this.fqcn.uqcn}.ts"


    val selectOptionsRenderedTypescriptFilePath = "/app/gen-components/${this.fqcn.packageName.asTypescriptDirs()}/${this.selectOptionsUqcn}.ts"


    val hasDisplayName = enumValueDefs.any { it.displayName != null }


    val defaultFormFieldValue = "$uqcn.${(enumValueDefs.firstOrNull { it.isDefaultFormValue }?.name ?: enumValueDefs.firstOrNull()?.name ?: error("No enum values defined for $uqcn"))}"


}
