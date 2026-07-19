package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.IsCreatableByUser
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser

data class ReferencedEntity(
    val fieldName: String,
    val displayName: String,
    val entityDef: EntityDef,
    val editableByUser: IsEditableByUser = IsEditableByUser.FALSE,
    val creatableByUser: IsCreatableByUser = IsCreatableByUser.FALSE,
    /**
     * Overrides the auto-generated name of the database index created for this side of a
     * many-to-many join. The auto-generated name is derived from the join entity's name and this
     * field's name, which can exceed PostgreSQL's identifier length limit; provide a shorter name
     * explicitly here when that happens.
     */
    val indexName: String? = null,
)
