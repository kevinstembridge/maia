package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class EntityDetailDtoEndpointRenderer(
    private val entityDetailViewDef: EntityDetailViewDef
) : AbstractKotlinRenderer(
    entityDetailViewDef.endpointClassDef
) {


    init {

        val serviceFqcn = this.entityDetailViewDef.serviceClassDef.fqcn
        addConstructorArg(aClassField("service", serviceFqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function fetch`()

    }


    private fun `render function fetch`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)

        appendLine("""
            |
            |
            |    @GetMapping("${entityDetailViewDef.fetchApiUrlForKotlin}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun fetch(@PathVariable id: DomainId): ${entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        return this.service.fetch(id)
            |
            |    }""".trimMargin()

        )

    }


}
