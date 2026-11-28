package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterHazelcastSourceDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


/**
 * Renders the single fetch-all GET of a Client-side blotter. It takes no filter, sort or paging parameters, and there
 * is no count endpoint.
 */
class HazelcastBlotterEndpointRenderer(
    private val blotterDef: BlotterDef
) : AbstractKotlinRenderer(
    blotterDef.searchDtoDef.searchEndpointClassDef
) {


    init {

        addConstructorArg(aClassField("searchService", blotterDef.searchDtoDef.searchServiceFqcn).privat().build())

    }


    override fun renderFunctions() {

        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)

        append("""
            |
            |
            |    @GetMapping("${blotterDef.searchDtoDef.findAllApiUrl}", produces = [MediaType.APPLICATION_JSON_VALUE])${preAuthorizeLine()}
            |    fun fetchAll(): List<${blotterDef.searchDtoDef.uqcn}> {
            |
            |        return this.searchService.fetchAll()
            |
            |    }
            |""".trimMargin())

    }


    private fun preAuthorizeLine(): String {

        val authority = (blotterDef.blotterSourceDef as? BlotterHazelcastSourceDef)?.searchableDtoDef?.hazelcastSpec?.authority
            ?: return ""

        addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
        return "\n            |    @PreAuthorize(\"hasAuthority('${authority.name}')\")"

    }


}
