package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions

class FetchForEditDtoDef(
    packageName: PackageName,
    entityBaseName: EntityBaseName,
    moduleName: ModuleName?,
    entityFieldDefs: List<EntityFieldDef>,
    manyToManyFieldDefs: List<ManyToManySearchableDtoFieldDef> = emptyList(),
    rootEntityDef: EntityDef? = null
) {


    private val moduleNameString = if (moduleName != null) "${StringFunctions.toKebabCase(moduleName.value)}/" else ""


    private val fetchForEditDtoClassFields = entityFieldDefs.map { it.classFieldDef }.map { classFieldDef ->

        if (classFieldDef.fieldType is ForeignKeyFieldType) {
            classFieldDef.asPkAndNameDtoClassFieldDef
        } else {
            classFieldDef
        }

    }.plus(manyToManyFieldDefs.map { it.classFieldDef })


    val dtoDef = DtoDefBuilder(
        packageName,
        DtoBaseName("${entityBaseName}FetchForEdit"),
        DtoSuffix("Dto"),
        fetchForEditDtoClassFields
    ).build()


    val uqcn = dtoDef.uqcn


    val rowMapperClassDef = dtoDef.rowMapperClassDef


    val rowMapperFieldDefs: List<RowMapperFieldDef> = entityFieldDefs.map { entityFieldDef ->

        if (entityFieldDef.foreignKeyFieldDef != null) {

            ForeignKeyRowMapperFieldDef(
                entityFieldDef.foreignKeyFieldDef,
                entityFieldDef.classFieldName,
                entityFieldDef.nullability
            )

        } else {

            EntityFieldRowMapperFieldDef(entityFieldDef.classFieldName, entityFieldDef)

        }

    }.plus(
        if (rootEntityDef != null) manyToManyFieldDefs.map { ManyToManyRowMapperFieldDef(it, rootEntityDef) }
        else emptyList()
    ).sortedBy { it.classFieldName.value }


    val rowMapperDef = RowMapperDef(
        dtoDef.fqcn,
        rowMapperFieldDefs,
        dtoDef.rowMapperClassDef,
        isForEditDto = true,
        compositeIdFields = emptyList()
    )


    val endpointUrl = "/api/$moduleNameString${entityBaseName.toKebabCase()}/fetch-for-edit"


    private val typescriptFilePathWithoutSuffix = "app/gen-components/${packageName.asTypescriptDirs()}/${uqcn}"


    val typescriptImport = TypescriptImport(uqcn.value, "@$typescriptFilePathWithoutSuffix")


}
