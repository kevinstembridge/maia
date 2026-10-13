package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class EntityPkAndNameDef(
    val packageName: PackageName,
    val dtoBaseName: DtoBaseName,
    val pkEntityFieldDef: EntityFieldDef,
    val nameEntityFieldDef: EntityFieldDef,
    val entityRepoClassDef: ClassDef,
    val shortcodeEntityFieldDef: EntityFieldDef? = null
) {


    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName.withSuffix("PkAndName"),
        DtoSuffix("Dto"),
        listOfNotNull(
            aClassField("id", pkEntityFieldDef.fieldType).build(),
            aClassField("name", nameEntityFieldDef.fieldType).build(),
            shortcodeEntityFieldDef?.let { aClassField("shortcode", it.fieldType).build() },
        )
    ).build()


    val pkAndNameDtoFqcn = dtoDef.fqcn


    val rowMapperDef = RowMapperDef(
        dtoDef.fqcn,
        listOfNotNull(
            EntityFieldRowMapperFieldDef(ClassFieldName("id"), pkEntityFieldDef, pkEntityFieldDef.tableColumnName.value),
            EntityFieldRowMapperFieldDef(ClassFieldName("name"), nameEntityFieldDef, nameEntityFieldDef.tableColumnName.value),
            shortcodeEntityFieldDef?.let { EntityFieldRowMapperFieldDef(ClassFieldName("shortcode"), it, it.tableColumnName.value) }
        ),
        dtoDef.rowMapperClassDef,
        isForEditDto = true,
        compositeIdFields = emptyList()
    )


    val dtoUqcn = dtoDef.uqcn


    /**
     * Kotlin source for constructing the DTO from an entity held in a variable named [entityVar].
     */
    fun constructionFrom(entityVar: String, indent: String): String {

        val args = listOfNotNull(
            pkEntityFieldDef,
            nameEntityFieldDef,
            shortcodeEntityFieldDef
        ).joinToString(",\n") { "$indent    $entityVar.${it.classFieldName}" }

        return "$dtoUqcn(\n$args\n$indent)"

    }


    val pkAndNameDtoImportStatement = "import { ${pkAndNameDtoFqcn.uqcn} } from '@${GeneratedTypescriptDir.forPackage(pkAndNameDtoFqcn.packageName)}/${pkAndNameDtoFqcn.uqcn}';"


    val pkAndNameDtoTypescriptImport = TypescriptImport(pkAndNameDtoFqcn.uqcn.value, "@${GeneratedTypescriptDir.forPackage(pkAndNameDtoFqcn.packageName)}/${pkAndNameDtoFqcn.uqcn}")


}
