package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class ForeignKeyReferencesEndpointRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.foreignKeyReferencesEndpointClassDef) {


    init {

        val serviceFqcn = this.entityDef.foreignKeyReferencesServiceClassDef.fqcn
        addConstructorArg(ClassFieldDef.aClassField("service", serviceFqcn).build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.SLF4J_LOGGER_FACTORY)

        blankLine()
        appendLine("    private val logger = LoggerFactory.getLogger(${this.classDef.uqcn}::class.java)")

    }


    override fun renderFunctions() {

        renderFunction_checkForeignKeyReferences()

    }


    private fun renderFunction_checkForeignKeyReferences() {

        addImportFor(Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)

        blankLine()
        blankLine()
        appendLine("    @GetMapping(\"${this.entityDef.checkForeignKeyReferencesEndpointUrl}/{id}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun checkForeignKeyReferences(@PathVariable(\"id\") id: DomainId): ${Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO.uqcn} {")
        blankLine()
        appendLine("        return this.service.checkForeignKeyReferences(id)")
        blankLine()
        appendLine("    }")

    }


}
