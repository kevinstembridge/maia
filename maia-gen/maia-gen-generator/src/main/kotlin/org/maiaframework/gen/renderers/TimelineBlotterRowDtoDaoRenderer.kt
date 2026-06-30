package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.jdbc.JdbcCompatibleType
import org.maiaframework.lang.text.StringFunctions


class TimelineBlotterRowDtoDaoRenderer(
    private val def: TimelineBlotterDef
) : AbstractKotlinRenderer(
    def.daoClassDef
) {


    init {

        addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS).privat().build())

    }


    override fun renderFunctions() {

        addImportFor(def.rowMapperClassDef.fqcn)
        addImportFor(def.metaClassDef.fqcn)

        `render the dtoRowMapper field`()
        `render the searchModelConverter field`()
        `render the search function`()
        `render the count function`()

    }


    private fun `render the dtoRowMapper field`() {

        blankLine()
        blankLine()
        appendLine("    private val dtoRowMapper = ${def.rowMapperUqcn}()")

    }


    private fun `render the searchModelConverter field`() {

        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL_CONVERTER)

        blankLine()
        blankLine()
        appendLine("    private val searchModelConverter = AgGridSearchModelConverter(")
        appendLine("        ${def.metaUqcn}::fieldNameToColumnName,")
        appendLine("        ${def.metaUqcn}::fieldNameToJdbcType")
        appendLine("    )")

    }


    private fun `render the search function`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val tripleQ = "\"\"\""

        append($$"""
            |
            |
            |    fun search(
            |       entityId: DomainId,
            |       searchModel: AgGridSearchModel
            |    ): SearchResultPage<$${def.rowDtoUqcn}> {
            |
            |        val sqlParams = SqlParams()
            |        sqlParams.addValue("entityId", entityId)
            |        val whereClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams)
            |        val offsetAndLimitClause = this.searchModelConverter.buildOffsetAndLimitFor(searchModel)
            |        val orderByClause = this.searchModelConverter.buildOrderByClause(searchModel)
            |
            |        val unionSql = buildUnionSql()
            |
            |        val sqlForTotalCount = $$tripleQ
            |            SELECT count(*)
            |            FROM (${unionSql}) AS timeline
            |            $whereClause
            |            $$tripleQ.trimIndent()
            |
            |        val sqlForPage = $$tripleQ
            |            SELECT *
            |            FROM (${unionSql}) AS timeline
            |            $whereClause
            |            $orderByClause
            |            $offsetAndLimitClause
            |            $$tripleQ.trimIndent()
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


    private fun `render the count function`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val tripleQ = "\"\"\""

        append($$"""
            |
            |
            |    fun count(
            |        entityId: DomainId,
            |        searchModel: AgGridSearchModel
            |    ): Long {
            |
            |        val sqlParams = SqlParams()
            |        sqlParams.addValue("entityId", entityId)
            |        val whereClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams)
            |
            |        val unionSql = buildUnionSql()
            |
            |        val sqlForTotalCount = $$tripleQ
            |            SELECT count(*)
            |            FROM (${unionSql}) AS timeline
            |            $whereClause
            |            $$tripleQ.trimIndent()
            |
            |        return this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
            |
            |    }
            |""".trimMargin())

    }


    override fun renderInnerClasses() {

        renderBuildUnionSqlFunction()

    }


    private fun renderBuildUnionSqlFunction() {

        val tripleQ = "\"\"\""
        val historyTable = def.historyTableSchemaAndTable

        val entityFieldSelects = def.entityHistoryColumns.joinToString(", ") { col ->
            val colName = StringFunctions.toSnakeCase(col.classFieldDef.classFieldName.value)
            "lmh.$colName"
        }
        val entityFieldSelectClause = if (entityFieldSelects.isNotEmpty()) ", $entityFieldSelects" else ""

        val joinNullSelects = def.entityHistoryColumns.joinToString(", ") { col ->
            val colName = StringFunctions.toSnakeCase(col.classFieldDef.classFieldName.value)
            val pgCast = jdbcTypeToPgCast(col.classFieldDef.fieldType.jdbcCompatibleType)
            "NULL::$pgCast AS $colName"
        }
        val joinNullSelectClause = if (joinNullSelects.isNotEmpty()) ", $joinNullSelects" else ""

        val joinArms = buildJoinArms(joinNullSelectClause)

        append("""
            |
            |
            |    private fun buildUnionSql(): String {
            |
            |        return $tripleQ
            |            SELECT
            |                lmh.last_modified_timestamp_utc AS event_timestamp,
            |                'ENTITY_CHANGE' AS event_type,
            |                lmh.change_type,
            |                lmh.version$entityFieldSelectClause${buildJoinNullsForEntityArm()}
            |            FROM $historyTable lmh
            |            WHERE lmh.id = :entityId
            |$joinArms
            |            $tripleQ.trimIndent()
            |
            |    }
            |""".trimMargin())

    }


    private fun buildJoinNullsForEntityArm(): String {

        if (def.joinDefs.isEmpty()) return ""

        return def.joinDefs.joinToString("") { joinDef ->
            ",\n                NULL::uuid AS ${joinDef.rightFkSqlAlias},\n                NULL::text AS ${joinDef.displayFieldSqlAlias}"
        }

    }


    private fun buildJoinArms(joinNullSelectClause: String): String {

        if (def.joinDefs.isEmpty()) return ""

        return def.joinDefs.joinToString("\n") { currentJoinDef ->
            val joinTable = currentJoinDef.joinTableSchemaAndTable
            val rightTable = currentJoinDef.rightEntitySchemaAndTable
            val entityFkCol = currentJoinDef.entityFkColumnName
            val rightFkCol = currentJoinDef.rightFkColumnName

            val joinColumnSelects = def.joinDefs.joinToString("") { joinDef ->
                if (joinDef == currentJoinDef) {
                    ",\n                j.${joinDef.rightFkColumnName} AS ${joinDef.rightFkSqlAlias},\n                r.${joinDef.rightEntityDisplayFieldColumnName} AS ${joinDef.displayFieldSqlAlias}"
                } else {
                    ",\n                NULL::uuid AS ${joinDef.rightFkSqlAlias},\n                NULL::text AS ${joinDef.displayFieldSqlAlias}"
                }
            }

            """            UNION ALL
            SELECT
                lower(j.effective_range) AS event_timestamp,
                'JOIN_ADDED' AS event_type,
                NULL::varchar AS change_type,
                NULL::bigint AS version$joinNullSelectClause$joinColumnSelects
            FROM $joinTable j
            JOIN $rightTable r ON r.id = j.$rightFkCol
            WHERE j.$entityFkCol = :entityId

            UNION ALL
            SELECT
                upper(j.effective_range) AS event_timestamp,
                'JOIN_REMOVED' AS event_type,
                NULL::varchar AS change_type,
                NULL::bigint AS version$joinNullSelectClause$joinColumnSelects
            FROM $joinTable j
            JOIN $rightTable r ON r.id = j.$rightFkCol
            WHERE j.$entityFkCol = :entityId
              AND upper(j.effective_range) IS NOT NULL"""
        }

    }


    private fun jdbcTypeToPgCast(type: JdbcCompatibleType): String {

        return when (type) {
            JdbcCompatibleType.integer, JdbcCompatibleType.integer_array -> "integer"
            JdbcCompatibleType.bigint -> "bigint"
            JdbcCompatibleType.smallint, JdbcCompatibleType.smallint_array -> "smallint"
            JdbcCompatibleType.decimal, JdbcCompatibleType.decimal_array -> "numeric"
            JdbcCompatibleType.text, JdbcCompatibleType.text_array, JdbcCompatibleType.varchar -> "text"
            JdbcCompatibleType.uuid, JdbcCompatibleType.uuid_array -> "uuid"
            JdbcCompatibleType.boolean, JdbcCompatibleType.boolean_array -> "boolean"
            JdbcCompatibleType.timestamp_with_time_zone, JdbcCompatibleType.timestamp, JdbcCompatibleType.timestamp_array -> "timestamptz"
            JdbcCompatibleType.date -> "date"
            JdbcCompatibleType.jsonb -> "jsonb"
        }

    }


}
