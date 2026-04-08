package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns


class EntityFilterRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.entityFilterClassDef) {


    override fun renderFunctions() {

        addImportFor(Fqcns.MAIA_SQL_PARAMS)
        blankLine()
        blankLine()
        this.appendLine("    fun whereClause(fieldConverter: ${entityDef.entityFieldConverterClassDef.uqcn}): String ")
        blankLine()
        blankLine()
        this.appendLine("    fun populateSqlParams(sqlParams: SqlParams)")

    }


}
