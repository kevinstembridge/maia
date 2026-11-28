package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


/**
 * Renders the service that rebuilds the Hazelcast map of a Hazelcast-backed Searchable DTO from the database, so an
 * operator can recover from a failed refresh without restarting. There is no endpoint for it in v1.
 */
class HazelcastRebuildServiceRenderer(
    private val searchableDtoDef: SearchableDtoDef
) : AbstractKotlinRenderer(
    searchableDtoDef.hazelcastRebuildServiceClassDef
) {


    init {

        addConstructorArg(aClassField("hazelcastRepo", searchableDtoDef.hazelcastRepoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        append("""
            |
            |
            |    /** Clears the map and repopulates it from the database. */
            |    fun rebuild() {
            |
            |        this.hazelcastRepo.populate()
            |
            |    }
            |""".trimMargin())

    }


}
