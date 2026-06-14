package org.maiaframework.gen.spec.definition

import org.maiaframework.lang.text.StringFunctions


data class ManyToManyEntityDef(
    val entityDef: EntityDef,
    val leftEntity: ReferencedEntity,
    val rightEntity: ReferencedEntity
) {


    fun otherSideFrom(entityDef: EntityDef): ReferencedEntity {

        if (entityDef == this.leftEntity.entityDef) return this.rightEntity
        if (entityDef == this.rightEntity.entityDef) return this.leftEntity

        throw IllegalArgumentException("The provided entityDef (${entityDef.entityBaseName}) does not reference this ManyToManyEntityDef (${this.entityDef.entityBaseName}).")

    }


    fun otherSideIdTableColumnName(entityDef: EntityDef): String {

        return StringFunctions.toSnakeCase(otherSideFrom(entityDef).fieldName) + "_id"

    }


    fun idTableColumnName(entityDef: EntityDef): String {

        if (entityDef == this.leftEntity.entityDef) return StringFunctions.toSnakeCase(this.leftEntity.fieldName) + "_id"
        if (entityDef == this.rightEntity.entityDef) return StringFunctions.toSnakeCase(this.rightEntity.fieldName) + "_id"

        throw IllegalArgumentException("The provided entityDef (${entityDef.entityBaseName}) does not reference this ManyToManyEntityDef (${this.entityDef.entityBaseName}).")

    }


    fun idFieldName(entityDef: EntityDef): String {

        if (entityDef == this.leftEntity.entityDef) return this.leftEntity.fieldName
        if (entityDef == this.rightEntity.entityDef) return this.rightEntity.fieldName

        throw IllegalArgumentException("The provided entityDef (${entityDef.entityBaseName}) does not reference this ManyToManyEntityDef (${this.entityDef.entityBaseName}).")

    }


    /**
     * A name suffix to disambiguate generated field/method names when `entityDef` has multiple
     * many-to-many associations whose other side resolves to the same field name (e.g. two
     * associations both pointing at a "right" entity).
     *
     * The first such association (in registration order) gets an empty suffix, preserving
     * existing naming for backwards compatibility. Subsequent ones get this association's join
     * entity base name appended, to disambiguate.
     */
    fun nameSuffixFor(entityDef: EntityDef): String {

        val otherSideFieldName = otherSideFrom(entityDef).fieldName

        val collidingAssociations = entityDef.manyToManyAssociations
            .filter { it.otherSideFrom(entityDef).fieldName == otherSideFieldName }

        return if (collidingAssociations.size > 1 && collidingAssociations.first() != this) {
            this.entityDef.entityBaseName.value
        } else {
            ""
        }

    }


    /**
     * The field name used for this association's data on `entityDef`'s FetchForEditDto.
     *
     * Normally this is "${otherSideFieldName}Entities", with [nameSuffixFor] appended (if any)
     * to disambiguate when `entityDef` has multiple many-to-many associations whose other side
     * resolves to the same field name.
     */
    fun fetchForEditFieldNameFor(entityDef: EntityDef): String {

        val otherSideFieldName = otherSideFrom(entityDef).fieldName
        return "${otherSideFieldName}${nameSuffixFor(entityDef)}Entities"

    }


}
