package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName

object EffectiveTimestampRendererHelper {

    private const val EFFECTIVE_FROM_COLUMN = "effective_from"
    private const val EFFECTIVE_TO_COLUMN = "effective_to"
    private const val EFFECTIVE_RANGE_COLUMN = "effective_range"

    const val EFFECTIVE_RANGE_WHERE_CLAUSE = "effective_range @> current_timestamp"


    fun usesEffectiveRange(entityDef: EntityDef): Boolean = entityDef.hasEffectiveTimestamps


    /**
     * Replaces "effective_from" with "effective_range" in place and drops "effective_to".
     */
    fun collapseEffectiveColumns(entityDef: EntityDef, columnNames: List<String>): List<String> {

        if (!usesEffectiveRange(entityDef)) {
            return columnNames
        }

        return columnNames
            .map { if (it == EFFECTIVE_FROM_COLUMN) EFFECTIVE_RANGE_COLUMN else it }
            .filterNot { it == EFFECTIVE_TO_COLUMN }

    }


    /**
     * Replaces ":effectiveFrom" with "tstzrange(:effectiveFrom, :effectiveTo)" in place and drops ":effectiveTo".
     */
    fun collapseEffectiveValuePlaceholders(entityDef: EntityDef, placeholders: List<String>): List<String> {

        if (!usesEffectiveRange(entityDef)) {
            return placeholders
        }

        return placeholders
            .map { if (it == ":effectiveFrom") "tstzrange(:effectiveFrom, :effectiveTo)" else it }
            .filterNot { it == ":effectiveTo" }

    }


    /**
     * "select *" plus the effective_range -> effectiveFrom/effectiveTo projection, for entities
     * with hasEffectiveTimestamps. Entities without it (incl. hasEffectiveLocalDates) get plain "select *".
     */
    fun selectStarClause(entityDef: EntityDef): String =
        if (usesEffectiveRange(entityDef)) {
            "select *, lower(effective_range) as effective_from, upper(effective_range) as effective_to"
        } else {
            "select *"
        }


    /**
     * SQL expression backing an effectiveFrom/effectiveTo FieldFilter. For hasEffectiveTimestamps
     * entities, effectiveFrom/effectiveTo are derived from the effective_range column via
     * lower()/upper() rather than being real columns. All other fields use their table column name.
     */
    fun fieldFilterColumnExpression(entityDef: EntityDef, fieldDef: EntityFieldDef): String {

        if (!usesEffectiveRange(entityDef)) {
            return fieldDef.tableColumnName.value
        }

        return when (fieldDef.classFieldName) {
            ClassFieldName.effectiveFrom -> "lower($EFFECTIVE_RANGE_COLUMN)"
            ClassFieldName.effectiveTo -> "upper($EFFECTIVE_RANGE_COLUMN)"
            else -> fieldDef.tableColumnName.value
        }

    }


}
