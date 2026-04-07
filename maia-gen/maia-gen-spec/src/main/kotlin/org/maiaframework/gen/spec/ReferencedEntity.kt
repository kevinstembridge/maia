package org.maiaframework.gen.spec

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser


data class ReferencedEntity(
    val fieldName: String,
    val displayName: String,
    val entityDef: EntityDef,
    val editableByUser: IsEditableByUser = IsEditableByUser.FALSE
)
