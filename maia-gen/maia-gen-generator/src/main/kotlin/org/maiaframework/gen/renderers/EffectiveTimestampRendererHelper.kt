package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef

object EffectiveTimestampRendererHelper {

    private const val EFFECTIVE_FROM_COLUMN = "effective_from"
    private const val EFFECTIVE_TO_COLUMN = "effective_to"
    private const val EFFECTIVE_RANGE_COLUMN = "effective_range"

    const val EFFECTIVE_RANGE_WHERE_CLAUSE = "effective_range @> current_timestamp"


    fun usesEffectiveRange(entityDef: EntityDef): Boolean = entityDef.hasEffectiveTimestamps.value


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


}
