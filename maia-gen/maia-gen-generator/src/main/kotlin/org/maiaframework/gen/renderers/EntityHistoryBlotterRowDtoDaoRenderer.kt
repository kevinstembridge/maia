package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class EntityHistoryBlotterRowDtoDaoRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractKotlinRenderer(
    def.daoClassDef
) {


    init {

        if (def.requiresJsonMapper) {
            addConstructorArg(aClassField("jsonMapper", Fqcns.JACKSON_JSON_MAPPER).privat().build())
        }

        addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL_CONVERTER)

        append("""
            |
            |
            |    private val dtoRowMapper = ${def.rowMapperUqcn}(${if (def.requiresJsonMapper) "jsonMapper" else ""})
            |
            |
            |    private val searchModelConverter = ${Fqcns.MAIA_AG_GRID_SEARCH_MODEL_CONVERTER.uqcn}(
            |        ${def.metaUqcn}::fieldNameToColumnName,
            |        ${def.metaUqcn}::fieldNameToJdbcType
            |    )
            |""".trimMargin())

    }


    override fun renderFunctions() {

        `render function search`()
        `render function count`()

    }


    private fun `render function search`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val table = def.historyTableSchemaAndTable
        val selectColumns = def.blotterColumns
            .sortedBy { it.classFieldDef.classFieldName.value }
            .joinToString(",\n                ") { col ->
                "$table.${col.tableColumnName.value} as ${col.classFieldDef.classFieldName.value}"
            }

        append("""
            |
            |
            |    fun search(entityId: DomainId, searchModel: AgGridSearchModel): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        val sqlParams = SqlParams()
            |        sqlParams.addValue("entityId", entityId)
            |        val entityIdFilter = "$table.id = :entityId"
            |        val whereClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams, entityIdFilter)
            |        val offsetAndLimitClause = this.searchModelConverter.buildOffsetAndLimitFor(searchModel)
            |        val orderByClause = this.searchModelConverter.buildOrderByClause(searchModel)
            |
            |        val sqlForTotalCount = ${"\"\"\""}
            |            select count(*)
            |            from $table
            |            ${"$"}whereClause
            |            ${"\"\"\""}.trimIndent()
            |
            |        val sqlForPage = ${"\"\"\""}
            |            select
            |                $selectColumns
            |            from $table
            |            ${"$"}whereClause
            |            ${"$"}orderByClause
            |            ${"$"}offsetAndLimitClause
            |            ${"\"\"\""}.trimIndent()
            |
            |        val totalResultCount = this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
            |        val results = this.jdbcOps.queryForList(sqlForPage, sqlParams, this.dtoRowMapper)
            |        val endRow = searchModel.endRow
            |        val limit = if (endRow == null) null else (endRow - searchModel.startRow)
            |
            |        return SearchResultPage(
            |            results,
            |            totalResultCount,
            |            searchModel.startRow,
            |            limit
            |        )
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val table = def.historyTableSchemaAndTable

        append("""
            |
            |
            |    fun count(entityId: DomainId, searchModel: AgGridSearchModel): Long {
            |
            |        val sqlParams = SqlParams()
            |        sqlParams.addValue("entityId", entityId)
            |        val entityIdFilter = "$table.id = :entityId"
            |        val whereClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams, entityIdFilter)
            |
            |        val sqlForTotalCount = ${"\"\"\""}
            |            select count(*)
            |            from $table
            |            ${"$"}whereClause
            |            ${"\"\"\""}.trimIndent()
            |
            |        return this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
            |
            |    }
            |""".trimMargin())

    }


}
