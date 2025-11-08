package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns


class EntityFilterRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.entityFilterClassDef) {


    override fun renderFunctions() {

        when (entityDef.databaseType) {
            DatabaseType.MONGO -> renderFunctionsForMongoDatabase()
            DatabaseType.JDBC -> renderFunctionsForJdbcDatabase()
        }

    }


    private fun renderFunctionsForMongoDatabase() {

        addImportFor(Fqcns.BSON)

        blankLine()
        blankLine()
        appendLine("    fun asBson(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): Bson")

    }


    private fun renderFunctionsForJdbcDatabase() {

        addImportFor(Fqcns.MAHANA_SQL_PARAMS)

        blankLine()
        blankLine()
        appendLine("    fun whereClause(fieldConverter: ${this.entityDef.entityFieldConverterClassDef.uqcn}): String ")
        blankLine()
        blankLine()
        appendLine("    fun populateSqlParams(sqlParams: SqlParams)")

    }


}
