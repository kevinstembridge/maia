package org.maiaframework.gen.spec.definition

/**
 * At the time an EntityFieldDef is constructed, the enclosing
 * EntityDef has not yet been constructed. Some Entity fields
 * belong to abstract superclasses. This class wraps a given
 * Entity field with the concrete subclass that contains the field.
 */
data class EntityAndField(
    val entityDef: EntityDef,
    val entityFieldDef: EntityFieldDef,
    val referencedEntityField: EntityAndField?
) {


    val schemaAndTableName = entityDef.schemaAndTableName


    val tableName = entityDef.tableName


    val databaseColumnName = entityFieldDef.tableColumnName


    val classFieldName = entityFieldDef.classFieldName


    val referencedEntityFieldName = referencedEntityField?.classFieldName ?: "none"


    val referencedEntityFieldNotNull: EntityAndField
        get() {

            if (referencedEntityField == null) {
                throw IllegalStateException("Not expecting referencedEntityField to be null. entity=${entityDef.entityBaseName}, entityFieldDef=$entityFieldDef")
            }

            return referencedEntityField

        }


}
