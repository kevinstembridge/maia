package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class JoinFetchDtoDef(
    packageName: PackageName,
    otherSideDisplayName: String,
    val nameTableColumnName: String,
    val joinEntitySchemaAndTableName: String,
    val otherSideIdTableColumnName: String,
    val thisSideIdTableColumnName: String,
    val otherSideEntitySchemaAndTableName: String,
) {

    private val dtoBaseName = DtoBaseName("${otherSideDisplayName}Join")

    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName,
        DtoSuffix("FetchDto"),
        listOf(
            ClassFieldDef.aClassField("id", FieldTypes.domainId).build(),
            ClassFieldDef.aClassField("name", FieldTypes.string).build(),
            ClassFieldDef.aClassField("effectiveFrom", FieldTypes.instant).nullable().build(),
            ClassFieldDef.aClassField("effectiveTo", FieldTypes.instant).nullable().build(),
        )
    ).build()

    val uqcn = dtoDef.uqcn

    val fqcn = dtoDef.fqcn

    private val typescriptFilePathWithoutSuffix = "app/gen-components/${packageName.asTypescriptDirs()}/${uqcn}"

    val typescriptImport = TypescriptImport(uqcn.value, "@$typescriptFilePathWithoutSuffix")

    val typescriptRenderedFilePath = "$typescriptFilePathWithoutSuffix.ts"

}
