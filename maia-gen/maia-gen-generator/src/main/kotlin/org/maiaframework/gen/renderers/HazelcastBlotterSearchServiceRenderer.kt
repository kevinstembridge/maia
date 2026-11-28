package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterHazelcastSourceDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


/**
 * Renders the service behind a Client-side blotter: it returns the whole set of rows from the Hazelcast map, mapped to
 * the Blotter's row DTO. There is no server-side filter, sort or paging.
 */
class HazelcastBlotterSearchServiceRenderer(
    private val blotterDef: BlotterDef
) : AbstractKotlinRenderer(
    blotterDef.searchDtoDef.searchServiceClassDef
) {


    private val searchableDtoDef = (blotterDef.blotterSourceDef as BlotterHazelcastSourceDef).searchableDtoDef


    init {

        addConstructorArg(aClassField("hazelcastRepo", searchableDtoDef.hazelcastRepoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        val rowDtoUqcn = blotterDef.searchDtoDef.uqcn

        append("""
            |
            |
            |    fun fetchAll(): List<$rowDtoUqcn> {
            |
            |        return this.hazelcastRepo.findAll().map { dto ->
            |            $rowDtoUqcn(
            |""".trimMargin())

        blotterDef.blotterColumnFields.forEach { column ->
            appendLine("                ${column.dtoFieldName} = dto.${column.fieldPathInSourceData},")
        }

        append("""
            |            )
            |        }
            |
            |    }
            |""".trimMargin())

    }


}
