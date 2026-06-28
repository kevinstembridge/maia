package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.TimelineBlotterJoinDef


@MaiaDslMarker
class TimelineBlotterDefBuilder(
    private val entityDef: EntityDef,
    private val manyToManyEntityDefs: List<ManyToManyEntityDef>
) {


    private val joinDisplayFields: MutableMap<ManyToManyEntityDef, Pair<EntityDef, String>> = mutableMapOf()


    fun joinDisplayField(joinDef: ManyToManyEntityDef, fromEntityDef: EntityDef, fieldName: String) {
        joinDisplayFields[joinDef] = Pair(fromEntityDef, fieldName)
    }


    fun build(): TimelineBlotterDef {

        val joinDefs = manyToManyEntityDefs.map { m2m ->
            val (displayEntityDef, fieldName) = joinDisplayFields[m2m]
                ?: error("No display field configured for join '${m2m.entityDef.entityBaseName}'")
            TimelineBlotterJoinDef(m2m, displayEntityDef, fieldName, entityDef)
        }

        return TimelineBlotterDef(entityDef, joinDefs)

    }


}
