package org.maiaframework.gen.renderers

import org.maiaframework.gen.renderers.SqlParamFunctions.sqlParamAddFunctionName
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.SearchableDtoFieldDef
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ConstructorArg
import org.maiaframework.gen.spec.definition.lang.Fqcn

class SearchableDtoJdbcDaoRenderer(
    private val searchableDtoDef: SearchableDtoDef,
    modelDef: ModelDef
): AbstractKotlinRenderer(
    searchableDtoDef.dtoDaoClassDef
) {


    private val entityDef = searchableDtoDef.dtoRootEntityDef


    private val dtoUqcn = searchableDtoDef.uqcn


    private val searchModelFqcn = when (this.searchableDtoDef.searchModelType) {
        SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCH_MODEL
        SearchModelType.MAIA -> Fqcns.MAIA_SEARCH_MODEL
    }


    private val searchModelConverterFqcn = when (this.searchableDtoDef.searchModelType) {
        SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCH_MODEL_CONVERTER
        SearchModelType.MAIA -> Fqcns.MAIA_SEARCH_MODEL_CONVERTER
    }


    private val typeDiscriminators = modelDef.entityHierarchyFor(searchableDtoDef.dtoRootEntityDef).typeDiscriminators()


    private val foreignKeyFieldsSortedByDepth = searchableDtoDef.allFields
        .filter { it.isForeignKeyRef }
        .sortedBy { it.fieldPathLength }


    private val foreignKeyTableCount = foreignKeyFieldsSortedByDepth
        .distinctBy(referencedFieldOrTable())
        .groupBy { it.schemaAndTableName }
        .mapValues { it.value.size }


    private val selectColumns = selectColumns(foreignKeyTableCount)


    private val rootEntitySchemaAndTable = searchableDtoDef.schemaAndTableName


    private val joinClauses = joinClauses(foreignKeyFieldsSortedByDepth, foreignKeyTableCount)


    init {

        addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS) { privat() }.build())

        if (searchableDtoDef.hasAnyMapFields) {
            addConstructorArg(aClassField("jsonFacade", Fqcns.MAIA_JSON_FACADE).privat().build())
            addConstructorArg(aClassField("objectMapper", Fqcns.JACKSON_OBJECT_MAPPER).privat().build())
        }

        this.entityDef.configurableSchemaPropertyName?.let { propertyName ->

            val classFieldDef = aClassField("schemaName", Fqcn.STRING) { privat() }.build()

            val annotationDefs = if (entityDef.daoHasSpringAnnotation.value) {
                listOf(AnnotationDef(Fqcns.SPRING_VALUE_ANNOTATION, value = { "\"\\\${$propertyName}\"" }))
            } else {
                emptyList()
            }

            addConstructorArg(ConstructorArg(classFieldDef, annotationDefs))

        }

    }


    override fun renderPreClassFields() {

        renderObjectMapperClassField()
        renderSearchModelConverterClassField()
        renderTypeDiscriminatorExpressionClassField()

    }


    private fun renderObjectMapperClassField() {

        addImportFor(searchableDtoDef.dtoRowMapperClassDef.fqcn)

        val objectMapperParameter = if (searchableDtoDef.hasAnyMapFields) "objectMapper" else ""

        blankLine()
        blankLine()
        appendLine("    private val dtoRowMapper = ${searchableDtoDef.dtoRowMapperClassDef.uqcn}($objectMapperParameter)")

    }


    private fun renderSearchModelConverterClassField() {

        addImportFor(this.searchModelConverterFqcn)

        when (this.searchableDtoDef.searchModelType) {

            SearchModelType.AG_GRID -> {

                appendLine("""
                    |
                    |
                    |    private val searchModelConverter = ${searchModelConverterFqcn.uqcn}(
                    |            ${searchableDtoDef.metaClassDef.uqcn}::fieldNameToColumnName,
                    |            ${searchableDtoDef.metaClassDef.uqcn}::fieldNameToJdbcType
                    |    )        
                """.trimMargin())



            }

            SearchModelType.MAIA -> {

                appendLine("""
                    |
                    |
                    |    private val searchModelConverter = ${searchModelConverterFqcn.uqcn}(
                    |            ${searchableDtoDef.metaClassDef.uqcn}::fieldNameToColumnName
                    |    )        
                """.trimMargin())

            }

        }

    }


    private fun renderTypeDiscriminatorExpressionClassField() {

        when {

            typeDiscriminators.size == 1 -> {

                blankLine()
                appendLine("    private val typeDiscriminatorExpression = \"type_discriminator = '${typeDiscriminators.first()}'\"")

            }

            typeDiscriminators.size > 1 -> {

                val text = typeDiscriminators.joinToString(", ") { "'${it.value}'" }

                blankLine()
                appendLine("    private val typeDiscriminatorExpression = \"type_discriminator in ($text)\"")

            }

        }

    }


    override fun renderFunctions() {

        `render function findById`()
        `render function search`()
        `render function count`()
//        `render function buildWhereClauseFor`()
//        `render function buildExpressionFor`()
//        `render function buildOrderByClause`()
//        `render function offsetAndLimitFor`()

    }


    private fun `render function findById`() {

        if (searchableDtoDef.generateFindById.value == false) {
            return
        }


        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        blankLine()
        blankLine()
        appendLine("    fun findById(id: DomainId): $dtoUqcn? {")
        blankLine()
        appendLine("        val sqlParams = SqlParams().addValue(\"id\", id)")
        blankLine()
        appendLine("        val sql = \"\"\"")
        appendLine("            select ")
        renderStrings(selectColumns, indent = 16)
        newLine()
        appendLine("            from $rootEntitySchemaAndTable")
        joinClauses.forEach { appendLine("            $it") }
        appendLine("            where id = :id")
        appendLine("            \"\"\".trimIndent()")
        blankLine()
        appendLine("        return this.jdbcOps.queryForObjectOrNull(sql, sqlParams, this.dtoRowMapper)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function search`() {

        addImportFor(searchModelFqcn)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val typeDiscriminatorText = if (typeDiscriminators.isEmpty()) "" else ", typeDiscriminatorExpression"

        blankLine()
        blankLine()
        appendLine("    fun search(searchModel: ${searchModelFqcn.uqcn}): SearchResultPage<${dtoUqcn}> {")
        blankLine()
        appendLine("        val sqlParams = SqlParams()")
        appendLine("        val whereClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams$typeDiscriminatorText)")
        appendLine("        val offsetAndLimitClause = this.searchModelConverter.buildOffsetAndLimitFor(searchModel)")
        appendLine("        val orderByClause = this.searchModelConverter.buildOrderByClause(searchModel)")
        blankLine()
        appendLine("        val sqlForTotalCount = \"\"\"")
        appendLine("            select count(*)")
        appendLine("            from $rootEntitySchemaAndTable")
        joinClauses.forEach { appendLine("            $it") }
        appendLine("            \$whereClause")
        appendLine("            \"\"\".trimIndent()")
        blankLine()
        appendLine("        val sqlForPage = \"\"\"")
        appendLine("            select")
        renderStrings(selectColumns, indent = 16)
        newLine()
        appendLine("            from $rootEntitySchemaAndTable")
        joinClauses.forEach { appendLine("            $it") }
        appendLine("            \$whereClause")
        appendLine("            \$orderByClause")
        appendLine("            \$offsetAndLimitClause")
        appendLine("            \"\"\".trimIndent()")
        blankLine()
        appendLine("        val totalResultCount = this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)")
        appendLine("        val results = this.jdbcOps.queryForList(sqlForPage, sqlParams, this.dtoRowMapper)")
        appendLine("        val endRow = searchModel.endRow")
        appendLine("        val limit = if (endRow == null) null else (endRow - searchModel.startRow)")
        blankLine()
        appendLine("        return SearchResultPage(")
        appendLine("            results,")
        appendLine("            totalResultCount,")
        appendLine("            searchModel.startRow,")
        appendLine("            limit")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function count`() {

        addImportFor(searchModelFqcn)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val typeDiscriminatorText = if (typeDiscriminators.isEmpty()) "" else ", typeDiscriminatorExpression"

        blankLine()
        blankLine()
        appendLine("    fun count(searchModel: ${searchModelFqcn.uqcn}): Long {")
        blankLine()
        appendLine("        val sqlParams = SqlParams()")
        appendLine("        val whereClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams$typeDiscriminatorText)")
        blankLine()
        appendLine("        val sqlForTotalCount = \"\"\"")
        appendLine("            select count(*)")
        appendLine("            from $rootEntitySchemaAndTable")
        joinClauses.forEach { appendLine("            $it") }
        appendLine("            \$whereClause")
        appendLine("            \"\"\".trimIndent()")
        blankLine()
        appendLine("        return this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)")
        blankLine()
        appendLine("    }")

    }


    private fun joinClauses(
        foreignKeyFieldsSortedByDepth: List<SearchableDtoFieldDef>,
        foreignKeyTableCount: Map<String, Int>
    ): List<String> {

        val manyToManyJoinClauses = searchableDtoDef.manyToManyJoinEntityDefs
            .map { joinEntityDef ->
                """${joinEntityDef.joinType.sql} join ${joinEntityDef.entityDef.schemaAndTableName}
                    on ${entityDef.schemaAndTableName}.id = ${joinEntityDef.entityDef.schemaAndTableName}.${
                    joinEntityDef.entityDef.foreignKeyFieldForBaseName(
                        entityDef.superEntityBaseName
                    ).tableColumnName
                }
                    """.trimIndent()
            }

        val foreignKeyJoinClauses = foreignKeyFieldsSortedByDepth
            .filter { it.entityAndField.referencedEntityField != null }
            .distinctBy { it.entityAndField.referencedEntityFieldNotNull.classFieldName }
            .map { searchableDtoFieldDef ->

                val referencedEntityField = searchableDtoFieldDef.entityAndField.referencedEntityField!!
                val tableIsReferencedCount = foreignKeyTableCount[searchableDtoFieldDef.schemaAndTableName] ?: 0
                val referencedTableAlias = if (tableIsReferencedCount > 1) referencedEntityField.classFieldName else searchableDtoFieldDef.schemaAndTableName

                """inner join ${searchableDtoFieldDef.schemaAndTableName}${if (tableIsReferencedCount > 1) " ${referencedEntityField.classFieldName}" else ""}
                    on ${referencedEntityField.schemaAndTableName}.${referencedEntityField.databaseColumnName} = ${referencedTableAlias}.id""".trimIndent()

            }

        val joinClauses = manyToManyJoinClauses.plus(foreignKeyJoinClauses)

        return joinClauses

    }


    private fun selectColumns(foreignKeyTableCount: Map<String, Int>): List<String> {

        return searchableDtoDef.allFields
            .map { searchableDtoFieldDef ->

                val numberOfTimesTheForeignTableIsReferenced = foreignKeyTableCount[searchableDtoFieldDef.schemaAndTableName] ?: 0
                val foreignTableIsReferencedMoreThanOnce = numberOfTimesTheForeignTableIsReferenced > 1

                val tableOrAlias = if (searchableDtoFieldDef.isForeignKeyRef && foreignTableIsReferencedMoreThanOnce) {
                    searchableDtoFieldDef.entityAndField.referencedEntityFieldNotNull.classFieldName
                } else {
                    searchableDtoFieldDef.schemaAndTableName
                }

                "${tableOrAlias}.${searchableDtoFieldDef.databaseColumn} as ${searchableDtoFieldDef.classFieldName}"
            }.plus(selectTypeDiscriminatorColumn())
            .filterNotNull()

    }


    private fun referencedFieldOrTable(): (SearchableDtoFieldDef) -> String {
        return {
            it.entityAndField.referencedEntityField?.classFieldName?.value ?: it.entityAndField.schemaAndTableName
        }
    }


    private fun selectTypeDiscriminatorColumn(): String? {

        return when {
            typeDiscriminators.isNotEmpty() -> "${entityDef.schemaAndTableName}.type_discriminator as typeDiscriminator"
            else -> null
        }

    }


    private fun `render function buildWhereClauseFor`() {

        blankLine()
        blankLine()
        appendLine("    private fun buildWhereClauseFor(")
        appendLine("        searchModel: ${searchModelFqcn.uqcn},")
        appendLine("        sqlParams: SqlParams")
        appendLine("    ): String {")
        blankLine()

        if (typeDiscriminators.isEmpty()) {

            appendLine("        return searchModel.filterModel.mapIndexed { index, filterModelItem ->")
            blankLine()
            appendLine("            val whereOrAnd = if (index == 0) \"where\" else \"and\"")
            appendLine("            val expression = buildExpressionFor(filterModelItem, sqlParams)")
            blankLine()
            appendLine("            \"\$whereOrAnd \$expression\"")
            blankLine()
            appendLine("        }.joinToString(\" \")")

        } else {

            appendLine("        val expressions = searchModel.filterModel.map { filterModelItem ->")
            appendLine("            buildExpressionFor(filterModelItem, sqlParams)")
            appendLine("        }.plus(typeDiscriminatorExpression)")
            blankLine()
            appendLine("        return expressions.mapIndexed { index, expression ->")
            blankLine()
            appendLine("            val whereOrAnd = if (index == 0) \"where\" else \"and\"")
            appendLine("            \"\$whereOrAnd \$expression\"")
            blankLine()
            appendLine("        }.joinToString(\" \")")


        }

        blankLine()
        appendLine("    }")

    }


    private fun `render function buildExpressionFor`() {

        addImportFor(Fqcns.MAIA_FILTER_MODEL_ITEM)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)
        addImportFor(Fqcns.SQL_TYPES)

        blankLine()
        blankLine()
        appendLine("    private fun buildExpressionFor(")
        appendLine("        filterModelItem: FilterModelItem,")
        appendLine("        sqlParams: SqlParams")
        appendLine("    ): String {")
        blankLine()
        appendLine("        val classFieldName = filterModelItem.fieldPath")
        appendLine("        val dbColumnName = ${searchableDtoDef.metaClassDef.uqcn}.fieldNameToColumnName(classFieldName)")
        blankLine()
        appendLine("        val fieldType = filterModelItem.fieldType")
        appendLine("        val filterType = filterModelItem.filterType")
        blankLine()
        appendLine("        when (fieldType) {")
        blankLine()
        appendLine("            \"id\" -> {")
        appendLine("                sqlParams.addValue(classFieldName, filterModelItem.filter, Types.OTHER)")
        appendLine("                return \"\$dbColumnName = :\$classFieldName\"")
        appendLine("            }")
        appendLine("            \"text\" -> {")
        blankLine()
        appendLine("                val likeOrIlike = if (filterModelItem.caseSensitive) \"like\" else \"ilike\"")
        blankLine()
        appendLine("                when (filterType) {")
        blankLine()
        appendLine("                    \"equals\" -> {")
        appendLine("                        sqlParams.addValue(classFieldName, filterModelItem.filter)")
        appendLine("                        return \"\$dbColumnName = :\$classFieldName\"")
        appendLine("                    }")
        blankLine()
        appendLine("                    \"notEqual\" -> {")
        appendLine("                        sqlParams.addValue(classFieldName, filterModelItem.filter)")
        appendLine("                        return \"\$dbColumnName != :\$classFieldName\"")
        appendLine("                    }")
        blankLine()
        appendLine("                    \"contains\" -> {")
        appendLine("                        sqlParams.addValue(classFieldName, \"%\${filterModelItem.filter}%\")")
        appendLine("                        return \"\$dbColumnName \$likeOrIlike :\$classFieldName\"")
        appendLine("                    }")
        blankLine()
        appendLine("                    \"notContains\" -> {")
        appendLine("                        sqlParams.addValue(classFieldName, \"%\${filterModelItem.filter}%\")")
        appendLine("                        return \"\$dbColumnName not \$likeOrIlike :\$classFieldName\"")
        appendLine("                    }")
        blankLine()
        appendLine("                    \"startsWith\" -> {")
        appendLine("                        sqlParams.addValue(classFieldName, \"\${filterModelItem.filter}%\")")
        appendLine("                        return \"\$dbColumnName \$likeOrIlike :\$classFieldName\"")
        appendLine("                    }")
        blankLine()
        appendLine("                    \"endsWith\" -> {")
        appendLine("                        sqlParams.addValue(classFieldName, \"%\${filterModelItem.filter}\")")
        appendLine("                        return \"\$dbColumnName \$likeOrIlike :\$classFieldName\"")
        appendLine("                    }")
        blankLine()
        appendLine("                    else -> throw IllegalArgumentException(\"Unknown filter type [\$filterType]\")")
        blankLine()
        appendLine("                }")
        blankLine()
        appendLine("            }")
        blankLine()
        appendLine("            \"date\" -> {")
        blankLine()
        appendLine("                when (filterType) {")
        blankLine()
        appendLine("                    \"equals\" -> {")
        blankLine()
        appendLine("                        val dateFrom = filterModelItem.dateFrom!!")
        appendLine("                        val dateFromParamName = \"\${dbColumnName}_dateFrom\"")
        blankLine()
        appendLine("                        val dateToParamName = \"\${dbColumnName}_dateTo\"")
        appendLine("                        val plusOneDay = dateFrom.plusDays(1)")
        blankLine()
        appendLine("                        sqlParams.addValue(dateFromParamName, dateFrom)")
        appendLine("                        sqlParams.addValue(dateToParamName, plusOneDay)")
        blankLine()
        appendLine("                        return \"\$dbColumnName >= :\$dateFromParamName and \$dbColumnName < :\$dateToParamName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"notEqual\" -> {")
        blankLine()
        appendLine("                        val dateFrom = filterModelItem.dateFrom!!")
        appendLine("                        val dateFromParamName = \"\${dbColumnName}_dateFrom\"")
        blankLine()
        appendLine("                        val dateToParamName = \"\${dbColumnName}_dateTo\"")
        appendLine("                        val plusOneDay = dateFrom.plusDays(1)")
        blankLine()
        appendLine("                        sqlParams.addValue(dateFromParamName, dateFrom)")
        appendLine("                        sqlParams.addValue(dateToParamName, plusOneDay)")
        blankLine()
        appendLine("                        return \"\$dbColumnName < :\$dateFromParamName or \$dbColumnName >= :\$dateToParamName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"greaterThan\" -> {")
        blankLine()
        appendLine("                        val dateFrom = filterModelItem.dateFrom!!")
        appendLine("                        val dateFromParamName = \"\${dbColumnName}_dateFrom\"")
        blankLine()
        appendLine("                        sqlParams.addValue(dateFromParamName, dateFrom.plusDays(1))")
        blankLine()
        appendLine("                        return \"\$dbColumnName >= :\$dateFromParamName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"lessThan\" -> {")
        blankLine()
        appendLine("                        val dateFrom = filterModelItem.dateFrom!!")
        appendLine("                        val dateFromParamName = \"\${dbColumnName}_dateFrom\"")
        blankLine()
        appendLine("                        sqlParams.addValue(dateFromParamName, dateFrom)")
        blankLine()
        appendLine("                        return \"\$dbColumnName < :\$dateFromParamName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"inRange\" -> {")
        blankLine()
        appendLine("                        val dateFrom = filterModelItem.dateFrom!!")
        appendLine("                        val dateTo = filterModelItem.dateTo!!")
        appendLine("                        val plusOneDay = dateTo.plusDays(1)")
        blankLine()
        appendLine("                        val dateFromParamName = \"\${dbColumnName}_dateFrom\"")
        appendLine("                        val dateToParamName = \"\${dbColumnName}_dateTo\"")
        blankLine()
        appendLine("                        sqlParams.addValue(dateFromParamName, dateFrom)")
        appendLine("                        sqlParams.addValue(dateToParamName, plusOneDay)")
        blankLine()
        appendLine("                        return \"\$dbColumnName >= :\$dateFromParamName and \$dbColumnName < :\$dateToParamName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    else -> throw IllegalArgumentException(\"Unknown filter type [\$filterType]\")")
        blankLine()
        appendLine("                }")
        blankLine()
        appendLine("            }")
        blankLine()
        appendLine("            \"number\" -> {")
        blankLine()
        appendLine("                when (filterType) {")
        blankLine()
        appendLine("                    \"equals\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        blankLine()
        appendLine("                        sqlParams.addValue(classFieldName, filter)")
        blankLine()
        appendLine("                        return \"\$dbColumnName = :\$classFieldName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"notEqual\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        blankLine()
        appendLine("                        sqlParams.addValue(classFieldName, filter)")
        blankLine()
        appendLine("                        return \"\$dbColumnName != :\$classFieldName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"greaterThan\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        blankLine()
        appendLine("                        sqlParams.addValue(classFieldName, filter)")
        blankLine()
        appendLine("                        return \"\$dbColumnName > :\$classFieldName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"greaterThanOrEqual\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        blankLine()
        appendLine("                        sqlParams.addValue(classFieldName, filter)")
        blankLine()
        appendLine("                        return \"\$dbColumnName >= :\$classFieldName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"lessThan\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        blankLine()
        appendLine("                        sqlParams.addValue(classFieldName, filter)")
        blankLine()
        appendLine("                        return \"\$dbColumnName < :\$classFieldName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"lessThanOrEqual\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        blankLine()
        appendLine("                        sqlParams.addValue(classFieldName, filter)")
        blankLine()
        appendLine("                        return \"\$dbColumnName <= :\$classFieldName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    \"inRange\" -> {")
        blankLine()
        appendLine("                        val filter = filterModelItem.filter!!.toDouble()")
        appendLine("                        val filterTo = filterModelItem.filterTo!!.toDouble()")
        blankLine()
        appendLine("                        val fromParamName = \"\${dbColumnName}_from\"")
        appendLine("                        val toParamName = \"\${dbColumnName}_to\"")
        blankLine()
        appendLine("                        sqlParams.addValue(fromParamName, filter)")
        appendLine("                        sqlParams.addValue(toParamName, filterTo)")
        blankLine()
        appendLine("                        return \"\$dbColumnName >= :\$fromParamName and \$dbColumnName < :\$toParamName\"")
        blankLine()
        appendLine("                    }")
        blankLine()
        appendLine("                    else -> throw IllegalArgumentException(\"Unknown filter type [\$filterType]\")")
        blankLine()
        appendLine("                }")
        blankLine()
        appendLine("            }")
        blankLine()
        appendLine("            else -> throw IllegalArgumentException(\"Unknown filter field type [\$fieldType]\")")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun `render function operatorFor`() {

        blankLine()
        blankLine()
        appendLine("    private fun operatorFor(filterType: String): String {")
        blankLine()
        appendLine("        return when(filterType) {")
        appendLine("            \"equals\" -> \"=\"")
        appendLine("            \"contains\" -> \"like\"")
        appendLine("            else -> throw IllegalArgumentException(\"Filter type [\$filterType] not supported yet.\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun `render function buildSqlParamsFor`() {

        addImportFor(Fqcns.MAIA_FILTER_MODEL_HELPER)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        blankLine()
        blankLine()
        appendLine("    private fun buildSqlParamsFor(searchModel: ${searchModelFqcn.uqcn}): SqlParams {")
        blankLine()
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        searchModel.filterModel.forEach { filterModelItem ->")
        blankLine()
        appendLine("            val fieldPath = filterModelItem.fieldPath")
        blankLine()
        appendLine("            when (fieldPath) {")

        this.searchableDtoDef.allFields.filter { it.isFilterable }.forEach { fieldDef ->
            val entityFieldDef = fieldDef.entityFieldDef
            val addFunctionName = sqlParamAddFunctionName(entityFieldDef.fieldType)
            val filterModelHelperFunctionName = entityFieldDef.filterModelHelperFunctionName()
            appendLine("                \"${fieldDef.classFieldName}\" -> sqlParams.$addFunctionName(fieldPath, FilterModelHelper.${filterModelHelperFunctionName}(filterModelItem))")
        }

        val dtoFieldNames = searchableDtoDef.allFields.filter { it.isFilterable }.map { it.classFieldName }

        appendLine("                else -> throw RuntimeException(\"Unknown fieldPath [\$fieldPath]. Expecting one of ${dtoFieldNames}.\")")
        appendLine("            }")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("        return sqlParams")
        blankLine()
        appendLine("    }")

    }


    private fun `render function buildOrderByClause`() {

        blankLine()
        blankLine()
        appendLine("    private fun buildOrderByClause(searchModel: ${searchModelFqcn.uqcn}): String {")
        blankLine()
        appendLine("        val sortModelItems = searchModel.sortModel")
        blankLine()
        appendLine("        if (sortModelItems.isEmpty()) {")
        appendLine("            return \"\"")
        appendLine("        }")
        blankLine()
        appendLine("        val clauses = sortModelItems.joinToString(\", \") { sortModelItem ->")
        blankLine()
        appendLine("            val dbColumnName = ${searchableDtoDef.metaClassDef.uqcn}.fieldNameToColumnName(sortModelItem.fieldPath)")
        appendLine("            \"\$dbColumnName \${sortModelItem.sortDirection}\"")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("        return \"order by \$clauses\"")
        blankLine()
        appendLine("    }")

    }


    private fun `render function offsetAndLimitFor`() {

        blankLine()
        blankLine()
        appendLine("    private fun buildOffsetAndLimitFor(searchModel: ${searchModelFqcn.uqcn}): String {")
        blankLine()
        appendLine("        val offset = searchModel.startRow")
        appendLine("        val endRow = searchModel.endRow")
        appendLine("        val limit = if (endRow == null) -1 else (endRow - offset)")
        blankLine()
        appendLine("        val offsetText = if (offset < 1) {")
        appendLine("            \"\"")
        appendLine("        } else {")
        appendLine("            \" offset \$offset\"")
        appendLine("        }")
        blankLine()
        appendLine("        val limitText = if (limit < 1) {")
        appendLine("            \"\"")
        appendLine("        } else {")
        appendLine("            \" limit \$limit\"")
        appendLine("        }")
        blankLine()
        appendLine("        return \"\$limitText\$offsetText\"")
        blankLine()
        appendLine("    }")

    }

}
