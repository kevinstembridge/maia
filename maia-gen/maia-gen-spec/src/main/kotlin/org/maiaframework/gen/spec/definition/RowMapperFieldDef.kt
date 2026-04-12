package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.Nullability


sealed class RowMapperFieldDef(
    val nullability: Nullability,
    val classFieldName: ClassFieldName
)


class EntityFieldRowMapperFieldDef(
    val entityFieldDef: EntityFieldDef,
    val resultSetFieldName: String? = null
) : RowMapperFieldDef(
    entityFieldDef.nullability,
    entityFieldDef.classFieldDef.classFieldName
)


class ForeignKeyRowMapperFieldDef(
    val foreignKeyFieldDef: ForeignKeyFieldDef,
    classFieldName: ClassFieldName,
    nullability: Nullability
) : RowMapperFieldDef(
    nullability,
    classFieldName
)


class ManyToManyRowMapperFieldDef(
    val manyToManySearchableDtoFieldDef: ManyToManySearchableDtoFieldDef,
    val rootEntityDef: EntityDef
) : RowMapperFieldDef(
    manyToManySearchableDtoFieldDef.nullability,
    manyToManySearchableDtoFieldDef.classFieldName
) {


    val otherSideEntity = manyToManySearchableDtoFieldDef.manyToManyEntityDef.otherSideFrom(rootEntityDef)


    val otherSideIdTableColumnName = manyToManySearchableDtoFieldDef.manyToManyEntityDef.idTableColumnName(otherSideEntity.entityDef)


    val thisSideIdTableColumnName = manyToManySearchableDtoFieldDef.manyToManyEntityDef.idTableColumnName(rootEntityDef)


    val entityPkAndNameDef = otherSideEntity.entityDef.entityPkAndNameDef


}
