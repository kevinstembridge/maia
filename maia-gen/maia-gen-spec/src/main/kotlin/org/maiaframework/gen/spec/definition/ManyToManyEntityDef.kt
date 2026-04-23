package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.ReferencedEntity


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

        return otherSideFrom(entityDef).fieldName + "_id"

    }


    fun idTableColumnName(entityDef: EntityDef): String {

        if (entityDef == this.leftEntity.entityDef) return this.leftEntity.fieldName + "_id"
        if (entityDef == this.rightEntity.entityDef) return this.rightEntity.fieldName + "_id"

        throw IllegalArgumentException("The provided entityDef (${entityDef.entityBaseName}) does not reference this ManyToManyEntityDef (${this.entityDef.entityBaseName}).")

    }


    fun idFieldName(entityDef: EntityDef): String {

        if (entityDef == this.leftEntity.entityDef) return this.leftEntity.fieldName
        if (entityDef == this.rightEntity.entityDef) return this.rightEntity.fieldName

        throw IllegalArgumentException("The provided entityDef (${entityDef.entityBaseName}) does not reference this ManyToManyEntityDef (${this.entityDef.entityBaseName}).")

    }


}
