package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.Nullability


sealed class RowMapperFieldDef(
    val nullability: Nullability,
    val classFieldName: ClassFieldName
)


class EntityFieldRowMapperFieldDef(
    classFieldName: ClassFieldName,
    val entityFieldDef: EntityFieldDef,
    val resultSetFieldName: String? = null
) : RowMapperFieldDef(
    entityFieldDef.nullability,
    classFieldName
)


class ForeignKeyRowMapperFieldDef(
    val foreignKeyFieldDef: ForeignKeyFieldDef,
    classFieldName: ClassFieldName,
    nullability: Nullability,
    /** Prefix of the result-set columns that carry the ref's id, name and shortcode, e.g. `alpha` for `alphaId`. */
    val resultSetPrefix: String = foreignKeyFieldDef.foreignKeyFieldName.value
) : RowMapperFieldDef(
    nullability,
    classFieldName
)


class ManyToManyRowMapperFieldDef(
    val manyToManySearchableDtoFieldDef: ManyToManySearchableDtoFieldDef,
    val rootEntityDef: EntityDef,
    val joinFetchDtoDef: JoinFetchDtoDef? = null
) : RowMapperFieldDef(
    manyToManySearchableDtoFieldDef.nullability,
    manyToManySearchableDtoFieldDef.classFieldName
) {


    val otherSideEntity = manyToManySearchableDtoFieldDef.manyToManyEntityDef.otherSideFrom(rootEntityDef)


    val otherSideIdTableColumnName = manyToManySearchableDtoFieldDef.manyToManyEntityDef.idTableColumnName(otherSideEntity.entityDef)


    val thisSideIdTableColumnName = manyToManySearchableDtoFieldDef.manyToManyEntityDef.idTableColumnName(rootEntityDef)


    val entityRefDef: EntityRefDef by lazy { otherSideEntity.entityDef.entityRefDef }


}
