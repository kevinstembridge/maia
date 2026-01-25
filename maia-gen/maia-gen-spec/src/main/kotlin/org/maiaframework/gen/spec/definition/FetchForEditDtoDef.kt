package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName

class FetchForEditDtoDef(
    packageName: PackageName,
    entityBaseName: EntityBaseName,
    entityFieldDefs: List<EntityFieldDef>,
    val databaseType: DatabaseType
) {


    private val fetchForEditDtoClassFields = entityFieldDefs.map { it.classFieldDef }.map { classFieldDef ->

        if (classFieldDef.fieldType is ForeignKeyFieldType) {
            classFieldDef.asIdAndNameDtoClassFieldDef
        } else {
            classFieldDef
        }

    }


    val dtoDef = DtoDefBuilder(
        packageName,
        DtoBaseName("${entityBaseName}FetchForEdit"),
        DtoSuffix("Dto"),
        fetchForEditDtoClassFields
    ).build()


    val uqcn = dtoDef.uqcn


    val rowMapperClassDef = dtoDef.rowMapperClassDef


    val rowMapperFieldDefs: List<RowMapperFieldDef> =
        entityFieldDefs.map { RowMapperFieldDef(it, it.nullability, it.classFieldName.value) }


}
