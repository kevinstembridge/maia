package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability


sealed class SearchableDtoFieldDef(
    val classFieldDef: ClassFieldDef,
    val sortIndexAndDirection: SortIndexAndDirection? = null
) {


    abstract fun copyWithFieldName(dtoFieldName: String): SearchableDtoFieldDef


    val classFieldName = classFieldDef.classFieldName


    val fieldSortModel: FieldSortModel? = sortIndexAndDirection?.let { FieldSortModel(classFieldDef.classFieldName, it) }


    abstract val displayName: FieldDisplayName?


    abstract val nullability: Nullability


}


class SimpleSearchableDtoFieldDef(
    val isFilterable: Boolean,
    val responseDtoFieldDef: ResponseDtoFieldDef,
    val entityAndField: EntityAndField,
    val fieldPath: FieldPath,
    sortIndexAndDirection: SortIndexAndDirection?,
    val esMappingOverride: EsDocMappingType? = null,
    /** Non-null when this field is a foreign key that an Elasticsearch-enabled searchable DTO exposes as the foreign Entity's ref. */
    val entityRefDef: EntityRefDef? = null
): SearchableDtoFieldDef(
    responseDtoFieldDef.classFieldDef,
    sortIndexAndDirection
) {


    val tableName = entityAndField.tableName


    val schemaAndTableName = entityAndField.schemaAndTableName


    val databaseColumn = entityAndField.databaseColumnName


    val fieldPathLength = fieldPath.length


    val entityFieldDef = entityAndField.entityFieldDef


    val isForeignKeyRef = this.fieldPathLength > 1


    override val nullability = this.responseDtoFieldDef.nullability


    override val displayName = this.entityFieldDef.classFieldDef.displayName


    override fun copyWithFieldName(dtoFieldName: String): SimpleSearchableDtoFieldDef {

        return SimpleSearchableDtoFieldDef(
            this.isFilterable,
            this.responseDtoFieldDef.copyWithFieldName(dtoFieldName),
            this.entityAndField,
            this.fieldPath,
            this.sortIndexAndDirection,
            this.esMappingOverride,
            this.entityRefDef
        )

    }


    /**
     * True for a foreign key, direct or the terminal field of a path, whose foreign Entity declares an Entity ref,
     * so that an Elasticsearch-enabled searchable DTO can expose it as that Entity's RefDto.
     *
     * Nullable foreign keys are not converted yet (a null ref needs an outer join, see the entity-ref spec).
     */
    fun canBeEntityRef(): Boolean {

        val foreignKeyFieldDef = entityFieldDef.foreignKeyFieldDef ?: return false
        val foreignEntityDef = foreignKeyFieldDef.foreignEntityDef

        return entityRefDef == null
            && nullability == Nullability.NOT_NULLABLE
            && foreignEntityDef.hasRefDtoDef
            && foreignEntityDef.primaryKeyFields.size == 1

    }


    /** This field typed as the foreign Entity's RefDto. See [canBeEntityRef]. */
    fun asEntityRef(): SimpleSearchableDtoFieldDef {

        val refDef = entityFieldDef.foreignKeyFieldDef!!.foreignEntityDef.entityRefDef

        return SimpleSearchableDtoFieldDef(
            this.isFilterable,
            this.responseDtoFieldDef.copyWithFieldType(FieldTypes.entityRef(refDef)),
            this.entityAndField,
            this.fieldPath,
            this.sortIndexAndDirection,
            this.esMappingOverride,
            refDef
        )

    }


}


class ManyToManySearchableDtoFieldDef(
    classFieldDef: ClassFieldDef,
    val manyToManyEntityDef: ManyToManyEntityDef,
    sortIndexAndDirection: SortIndexAndDirection? = null,
    override val nullability: Nullability,
    override val displayName: FieldDisplayName? = null
) : SearchableDtoFieldDef(
    classFieldDef,
    sortIndexAndDirection
) {


    override fun copyWithFieldName(dtoFieldName: String): ManyToManySearchableDtoFieldDef {

        return ManyToManySearchableDtoFieldDef(
            this.classFieldDef.withFieldName(dtoFieldName),
            this.manyToManyEntityDef,
            this.sortIndexAndDirection,
            this.nullability,
            this.displayName
        )

    }


}


class CompositeIdFieldDef(
    classFieldDef: ClassFieldDef
) : SearchableDtoFieldDef(
    classFieldDef,
    sortIndexAndDirection = null
) {


    override fun copyWithFieldName(dtoFieldName: String): SearchableDtoFieldDef {
        TODO("Not yet implemented")
    }


    override val displayName: FieldDisplayName = FieldDisplayName("ID")


    override val nullability: Nullability = Nullability.NOT_NULLABLE


}
