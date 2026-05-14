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

        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)

        if (entityDetailViewDef.entityDef.hasSurrogatePrimaryKey) {

            `render fetch for surrogate primary key`()

        } else if (entityDetailViewDef.entityDef.hasCompositePrimaryKey) {

            `render fetch for composite primary key`()

        } else {

            `render fetch for natural primary key`()

        }

    }


    private fun `render fetch for surrogate primary key`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        append("""
            |
            |
            |    @GetMapping("${entityDetailViewDef.fetchApiUrlForKotlin}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun fetch(@PathVariable id: DomainId): ${entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        return this.service.fetch(id)
            |
            |    }
            |""".trimMargin()

        )

    }


    private fun `render fetch for composite primary key`() {

        append("""
            |
            |
            |    @GetMapping("${entityDetailViewDef.fetchApiUrlForKotlin}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun fetch(@PathVariable id: String): ${entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        val primaryKey = ${entityDetailViewDef.entityDef.entityPkClassDef.uqcn}.from(id)
            |        return this.service.fetch(primaryKey)
            |
            |    }
            |""".trimMargin()

        )

    }


    private fun `render fetch for natural primary key`() {

        val fqcn = entityDetailViewDef.entityDef.primaryKeyFields.first().classFieldDef.fqcn
        val pkUqcn = fqcn.uqcn

        addImportFor(fqcn)

        append(
            """
            |
            |
            |    @GetMapping("${entityDetailViewDef.fetchApiUrlForKotlin}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun fetch(@PathVariable id: $pkUqcn): ${entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        return this.service.fetch(id)
            |
            |    }
            |""".trimMargin()
        )

    }


}
