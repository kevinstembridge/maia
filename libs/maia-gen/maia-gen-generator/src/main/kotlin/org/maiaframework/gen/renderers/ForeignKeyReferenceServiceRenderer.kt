package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class ForeignKeyReferenceServiceRenderer(
        private val entityDef: EntityDef,
        private val referencingEntityDefs: List<EntityDef>
) : AbstractKotlinRenderer(
        entityDef.foreignKeyReferencesServiceClassDef
) {


    init {

        this.referencingEntityDefs.forEach { referencingEntityDef ->
            addConstructorArg(ClassFieldDef.aClassField(referencingEntityDef.entityRepoFqcn.uqcn.firstToLower(), referencingEntityDef.entityRepoFqcn).privat().build())
        }

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.SLF4J_LOGGER_FACTORY)

        blankLine()
        blankLine()
        appendLine("    private val logger = LoggerFactory.getLogger(${this.classDef.uqcn}::class.java)")

    }


    override fun renderFunctions() {

        renderFunction_checkForeignKeyReferences()

    }


    private fun renderFunction_checkForeignKeyReferences() {

        addImportFor(Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO)
        addImportFor(Fqcns.MAHANA_DOMAIN_ID)

        blankLine()
        blankLine()
        appendLine("    fun checkForeignKeyReferences(id: DomainId): ${Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO.uqcn} {")

        this.referencingEntityDefs.forEach { referencingEntityDef ->

            val field = referencingEntityDef.allForeignKeyEntityFieldDefs.find { it.foreignKeyFieldDef?.foreignEntityBaseName == this.entityDef.entityBaseName }

            val fieldName = field!!.classFieldName

            blankLine()
            appendLine("        if (this.${referencingEntityDef.entityRepoFqcn.uqcn.firstToLower()}.existsBy${fieldName.firstToUpper()}(id)) {")
            appendLine("            return ${Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO.uqcn}(id, true, \"${referencingEntityDef.entityBaseName}\")")
            appendLine("        }")

        }

        blankLine()
        appendLine("        return ${Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO.uqcn}(id, false, null)")
        blankLine()
        appendLine("    }")

    }


}
