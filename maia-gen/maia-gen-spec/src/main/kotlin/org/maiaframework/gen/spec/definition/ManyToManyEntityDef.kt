package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.ReferencedEntity


data class ManyToManyEntityDef(
    val entityDef: EntityDef,
    val leftEntity: ReferencedEntity,
    val rightEntity: ReferencedEntity
)
