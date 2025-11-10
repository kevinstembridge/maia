package org.maiaframework.gen.renderers

import org.maiaframework.domain.ChangeType
import org.maiaframework.gen.renderers.SqlParamFunctions.renderSqlParamAddValueFor
import org.maiaframework.gen.renderers.SqlParamFunctions.sqlParamAddFunctionName
import org.maiaframework.gen.renderers.SqlParamFunctions.sqlParamMapperFunction
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.EntityIdAndNameDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.IndexDef
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.ConstructorArg
import java.time.Instant

class JdbcDaoRenderer(
    private val entityHierarchy: EntityHierarchy
): AbstractKotlinRenderer(
    entityHierarchy.entityDef.daoClassDefToRender
) {


    private val entityDef = entityHierarchy.entityDef


    private val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(entityDef.primaryKeyClassFields)


    private val primaryKeyFieldNamesCsv = fieldNamesCsv(entityDef.primaryKeyClassFields)


    private val mapOfPrimaryKeyFields = "mapOf(${entityDef.primaryKeyClassFields.joinToString(", ") { "\"${it.classFieldName}\" to ${it.classFieldName}" }})"


    private val foreignKeyTableCount = entityDef.allForeignKeyEntityFieldDefs
        .distinctBy(referencedFieldOrTable())
        .groupBy { it.foreignKeyFieldDef!!.foreignEntityDef.schemaAndTableName }
        .mapValues { it.value.size }


    private fun referencedFieldOrTable(): (EntityFieldDef) -> String {
        return {
            it.foreignKeyFieldDef?.foreignKeyFieldName?.value ?: it.foreignKeyFieldDef!!.foreignEntityDef.schemaAndTableName
        }
    }


    init {

        addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS).privat().build())

        addConstructorArg(aClassField("fieldConverter", entityDef.entityFieldConverterClassDef.fqcn).privat().build())

        if (entityHierarchy.requiresObjectMapper) {
            addConstructorArg(aClassField("jsonFacade", Fqcns.MAIA_JSON_FACADE).privat().build())
            addConstructorArg(aClassField("objectMapper", Fqcns.JACKSON_OBJECT_MAPPER).privat().build())
        }

        this.entityDef.configurableSchemaPropertyName?.let { propertyName ->

            val schemaNameClassFieldDef = aClassField("schemaName", Fqcns.STRING).privat().build()

            val annotationDefs = if (entityDef.daoHasSpringAnnotation.value) {
                listOf(AnnotationDef(Fqcns.SPRING_VALUE_ANNOTATION, value = { "\"\\\${$propertyName}\"" }))
            } else {
                emptyList()
            }

            addConstructorArg(ConstructorArg(schemaNameClassFieldDef, annotationDefs))

        }

        entityDef.historyEntityDef?.let {
            addConstructorArg(aClassField("historyDao", it.daoFqcn).privat().build())
        }

        if (this.entityHierarchy.hasSubclasses()) {
            this.entityHierarchy.concreteEntityDefs.forEach { concreteEntityDef ->
                concreteEntityDef.historyEntityDef?.let { historyEntityDef ->
                    addConstructorArg(aClassField("${historyEntityDef.entityBaseName.firstToLower()}Dao", historyEntityDef.daoFqcn).privat().build())
                }
            }
        }

    }


    override fun renderPreClassFields() {

        addImportFor(this.entityDef.rowMapperClassDef.fqcn)

        val objectMapperParameter = if (entityHierarchy.requiresObjectMapper) "objectMapper" else ""

        blankLine()
        blankLine()
        appendLine("    private val entityRowMapper = ${entityDef.rowMapperClassDef.uqcn}($objectMapperParameter)")

        val primaryKeyRowMapperDef = this.entityDef.primaryKeyRowMapperDef

        if (primaryKeyRowMapperDef != null) {

            blankLine()
            blankLine()

            addImportFor(primaryKeyRowMapperDef.classDef.fqcn)

            appendLine("    private val primaryKeyRowMapper = ${primaryKeyRowMapperDef.classDef.uqcn}()")

        }

        if (this.entityDef.isModifiable == false && this.entityDef.isHistoryEntity == false && this.entityDef.uniqueIndexDefs.isNotEmpty()) {
            addImportFor(Fqcns.MAIA_JDBC_ROW_MAPPER)
            blankLine()
            blankLine()
            appendLine("    private val idRowMapper = MaiaRowMapper { rs -> rs.readDomainId(\"id\") }")
        }

        if (entityDef.isConcrete && entityDef.entityCrudApiDef?.updateApiDef != null) {
            appendLine("    private val fetchForEditDtoRowMapper = ${entityDef.fetchForEditDtoRowMapperClassDef.uqcn}($objectMapperParameter)")
        }

    }


    override fun renderFunctions() {

        `render function insert`()
        `render function bulkInsert`()
        `render function bulkInsertOfCsvRecords`()
        `render function insertHistory`()
        `render function bulkInsertHistory`()
        `render function history`()
        `render function count`()
        `render function countWithFilter`()
        `render function findByPrimaryKey`()
        `render function findByPrimaryKeyOrNull`()
        `render finders for indexes`()
        `render findAll`()
        `render findAllEffective`()
        `render function findAllByFilter`()
        `render function findAllByFilterAsSequence`()
        `render function findIdsAsSequence`()
        `render function findAllPrimaryKeysAsSequence`()
        `render function findAllByFilterAndPageRequest`()
        `render function findAllAsSequence`()
        `render existsBy functions`()
        `render function fetchForEdit`()
        `render upserts for indexes`()
        `render function setFields`()
        `render function deleteByPrimaryKey`()
        `render function deleteAll`()
        `render deleteBy for indexes`()

    }


    private fun `render function insert`() {

        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        if (entityHierarchy.hasSubclasses()) {

            blankLine()
            blankLine()
            appendLine("    fun insert(entity: ${entityDef.entityUqcn}) {")
            blankLine()
            appendLine("        when (entity) {")

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->
                addImportFor(entityDef.entityClassDef.fqcn)
                appendLine("            is ${entityDef.entityUqcn} -> insert${entityDef.entityUqcn}(entity)")
            }

            appendLine("        }")
            blankLine()
            appendLine("    }")

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->
                `render function insertSubclass`(entityDef)
            }

        } else {

            blankLine()
            blankLine()
            appendLine("    fun insert(entity: ${entityDef.entityUqcn}) {")

            renderFunctionInsertFor(entityDef)

            blankLine()
            appendLine("    }")
        }

    }


    private fun `render function insertSubclass`(entityDef: EntityDef) {

        blankLine()
        blankLine()
        appendLine("    private fun insert${entityDef.entityUqcn}(entity: ${entityDef.entityUqcn}) {")
        renderFunctionInsertFor(entityDef)
        blankLine()
        appendLine("    }")

    }


    private fun renderFunctionInsertFor(entityDef: EntityDef) {

        blankLine()
        appendLine("        jdbcOps.update(")
        appendLine("            \"\"\"")
        appendLine("            insert into ${entityDef.schemaAndTableName} (")

        if (entityDef.typeDiscriminatorOrNull != null) {
            appendLine("                type_discriminator,")
        }

        renderStrings(entityDef.databaseColumnNames(), indent = 16)
        newLine()

        appendLine("            ) values (")

        if (entityDef.typeDiscriminatorOrNull != null) {

            if (entityHierarchy.hasSubclasses()) {
                appendLine("                :typeDiscriminator,")
            } else {
                appendLine("                '${entityDef.typeDiscriminator}',")
            }
        }

        val fieldNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.classFieldName.value}" }
        renderStrings(fieldNames, indent = 16)
        newLine()

        appendLine("            )")
        appendLine("            \"\"\".trimIndent(),")
        renderSqlParamsForEntity(entityDef, 12)
        appendLine("        )")

        if (entityDef.withVersionHistory.value) {
            addImportFor<ChangeType>()
            blankLine()
            appendLine("        insertHistory(entity, ChangeType.CREATE)")
        }
    }

    private fun `render function bulkInsert`() {

        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        blankLine()
        blankLine()
        appendLine("    fun bulkInsert(entities: List<${entityDef.entityUqcn}>) {")
        blankLine()
        appendLine("        jdbcOps.batchUpdate(")
        appendLine("            \"\"\"")
        appendLine("            insert into ${entityDef.schemaAndTableName} (")

        if (entityDef.typeDiscriminatorOrNull != null) {
            appendLine("                type_discriminator,")
        }

        val databaseColumnNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { it.tableColumnName.value }
        renderStrings(databaseColumnNames, indent = 16)
        newLine()
        appendLine("            ) values (")

        if (entityDef.typeDiscriminatorOrNull != null) {

            if (entityHierarchy.hasSubclasses()) {
                appendLine("                :typeDiscriminator,")
            } else {
                appendLine("                '${entityDef.typeDiscriminator}',")
            }

        }

        val fieldNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.classFieldName.value}" }
        renderStrings(fieldNames, indent = 16)
        newLine()
        appendLine("            )")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            entities.map { entity ->")
        renderSqlParamsForEntity(entityDef, 16)
        appendLine("            }")
        appendLine("        )")

        if (entityDef.withVersionHistory.value) {
            blankLine()
            appendLine("        bulkInsertHistory(entities, ChangeType.CREATE)")
        }

        blankLine()
        appendLine("    }")

    }


    private fun `render function bulkInsertOfCsvRecords`() {

        if (this.entityDef.isStagingEntity == false) {
            return
        }

        addImportFor<Instant>()
        addImportFor(Fqcns.MAIA_SQL_PARAMS)
        addImportFor(Fqcns.MAIA_CSV_PERSISTABLE_RECORD)

        blankLine()
        blankLine()
        appendLine("    fun bulkInsertOfCsvRecords(csvPersistableRecords: List<CsvPersistableRecord>) {")
        blankLine()
        appendLine("        jdbcOps.batchUpdate(")
        appendLine("            \"\"\"")
        appendLine("            insert into ${entityDef.schemaAndTableName} (")

        if (entityDef.typeDiscriminatorOrNull != null) {
            appendLine("                type_discriminator,")
        }

        val databaseColumnNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { it.tableColumnName.value }
        renderStrings(databaseColumnNames, indent = 16)
        newLine()
        appendLine("            ) values (")

        if (entityDef.typeDiscriminatorOrNull != null) {

            if (entityHierarchy.hasSubclasses()) {
                appendLine("                :typeDiscriminator,")
            } else {
                appendLine("                '${entityDef.typeDiscriminator}',")
            }

        }

        val fieldNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.tableColumnName}" }
        renderStrings(fieldNames, indent = 16)
        newLine()
        appendLine("            )")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            csvPersistableRecords.map { csvPersistableRecord ->")
        blankLine()
        appendLine("                val sqlParams = SqlParams()")
        appendLine("                sqlParams.addValue(\"id\", DomainId.newId())")
        appendLine("                sqlParams.addValue(\"c_ts\", Instant.now())")
        appendLine("                sqlParams.addValue(\"file_storage_id\", csvPersistableRecord.fileStorageId)")
        appendLine("                sqlParams.addValue(\"line_number\", csvPersistableRecord.lineNumber)")
        blankLine()
        appendLine("                csvPersistableRecord.fields.forEach { field ->")
        appendLine("                    sqlParams.addValue(field.tableColumnName, field.value)")
        appendLine("                }")
        blankLine()
        appendLine("                sqlParams")
        blankLine()
        appendLine("            }")
        appendLine("        )")

        if (entityDef.withVersionHistory.value) {
            blankLine()
            appendLine("        bulkInsertHistory(entities, ChangeType.CREATE)")
        }

        blankLine()
        appendLine("    }")

    }


    private fun `render function insertHistory`() {

        if (entityDef.historyEntityDef == null) {
            return
        }

        addImportFor<ChangeType>()

        if (entityHierarchy.hasSubclasses()) {

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->

                append("""
                    |
                    |
                    |    private fun insertHistory(entity: ${entityDef.entityUqcn}, changeType: ChangeType) {
                    |
                    |        insertHistory(entity, entity.version, changeType)
                    |
                    |    }
                    |
                    |
                    |    private fun insertHistory(entity: ${entityDef.entityUqcn}, version: Long, changeType: ChangeType) {
                    |
                    |        this.historyDao.insert(history(entity, version, changeType))
                    |
                    |    }
                    |""".trimMargin())

            }

        } else {

            append("""
                |
                |
                |    private fun insertHistory(entity: ${entityDef.entityUqcn}, changeType: ChangeType) {
                |
                |        insertHistory(entity, entity.version, changeType)
                |
                |    }
                |
                |
                |    private fun insertHistory(entity: ${entityDef.entityUqcn}, version: Long, changeType: ChangeType) {
                |
                |        this.historyDao.insert(history(entity, version, changeType))
                |
                |    }
                |""".trimMargin())

        }

    }


    private fun `render function bulkInsertHistory`() {

        if (entityDef.historyEntityDef == null) {
            return
        }

        addImportFor<ChangeType>()

        if (entityHierarchy.hasSubclasses()) {

            blankLine()
            blankLine()
            appendLine("    private fun bulkInsertHistory(entities: List<${entityDef.entityUqcn}>, changeType: ChangeType) {")
            blankLine()
            appendLine("        val entitiesByType: Map<String, List<${entityDef.entityUqcn}>> = entities.groupBy { entity ->")
            appendLine("                when (entity) {")

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->
                appendLine("                    is ${entityDef.entityUqcn} -> \"${entityDef.typeDiscriminator}\"")
            }

            appendLine("                    else -> throw RuntimeException(\"Not going to happen (tm)\")")
            appendLine("                }")
            appendLine("        }")

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->
                blankLine()
                appendLine("        val entities${entityDef.typeDiscriminator}: List<${entityDef.entityUqcn}> = entitiesByType[\"${entityDef.typeDiscriminator}\"] as? List<${entityDef.entityUqcn}> ?: emptyList()")
                appendLine("        val ${entityDef.historyEntityDef!!.entityUqcn.firstToLower()}List = entities${entityDef.typeDiscriminator}.map { history(it, it.version + 1, changeType) }")
                appendLine("        this.${entityDef.historyEntityDef!!.daoFqcn.uqcn.firstToLower()}.bulkInsert(${entityDef.historyEntityDef!!.entityUqcn.firstToLower()}List)")
            }

            blankLine()
            appendLine("    }")

        } else {

            append("""
                |
                |
                |    private fun bulkInsertHistory(entities: List<${entityDef.entityUqcn}>, changeType: ChangeType) {
                |
                |        val historyEntities = entities.map { history(it, it.version, changeType) }
                |        this.historyDao.bulkInsert(historyEntities)
                |
                |    }
                |""".trimMargin())

        }

    }


    private fun `render function history`() {

        val historyEntityDef = this.entityDef.historyEntityDef
            ?: return

        addImportFor<ChangeType>()

        if (entityHierarchy.hasSubclasses()) {

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->

                addImportFor(entityDef.historyEntityDef!!.entityFqcn)

                blankLine()
                blankLine()
                appendLine("    private fun history(")
                appendLine("        entity: ${entityDef.entityUqcn},")
                appendLine("        version: Long,")
                appendLine("        changeType: ChangeType")
                appendLine("    ): ${entityDef.historyEntityDef!!.entityUqcn} {")
                blankLine()

                if (entityDef.hasSurrogatePrimaryKey) {
                    appendLine("        val id = entity.id")
                }

                entityDef.allClassFieldsSorted.filterNot { it.classFieldName == ClassFieldName.id || it.classFieldName == ClassFieldName.version }.forEach { fd ->
                    appendLine("        val ${fd.classFieldName} = entity.${fd.classFieldName}")
                }

                EntityRendererHelper.renderCallToEntityConstructor(entityDef.historyEntityDef!!, indentSize = 8, renderer = this)

                blankLine()
                appendLine("    }")

            }

        } else {

            blankLine()
            blankLine()
            appendLine("    private fun history(")
            appendLine("        entity: ${this.entityDef.entityUqcn},")
            appendLine("        version: Long,")
            appendLine("        changeType: ChangeType")
            appendLine("    ): ${historyEntityDef.entityUqcn} {")
            blankLine()

            if (entityDef.hasSurrogatePrimaryKey) {
                appendLine("        val id = entity.id")
            }

            this.entityDef.allClassFieldsSorted.filterNot { it.classFieldName == ClassFieldName.id || it.classFieldName == ClassFieldName.version }.forEach { fd ->
                appendLine("        val ${fd.classFieldName} = entity.${fd.classFieldName}")
            }

            EntityRendererHelper.renderCallToEntityConstructor(historyEntityDef, indentSize = 8, renderer = this)

            blankLine()
            appendLine("    }")

        }

    }


    private fun renderSqlParamsForEntity(
        entityDef: EntityDef,
        indentSize: Int
    ) {

        val indent = "".padStart(indentSize, ' ')

        appendLine("${indent}SqlParams().apply {")

        if (entityDef.typeDiscriminatorOrNull != null && entityHierarchy.hasSubclasses()) {
            addImportFor(entityDef.metaClassDef.fqcn)
            appendLine("$indent    addValue(\"typeDiscriminator\", ${entityDef.metaClassDef.uqcn}.TYPE_DISCRIMINATOR)")
        }

        renderSqlParamsAddValueLinesFor("entity", entityDef, indentSize)

        appendLine("$indent}")

    }


    private fun `render function findByPrimaryKey`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        this.entityDef.primaryKeyClassFields.forEach { addImportFor(it.fieldType) }
        addImportFor(Fqcns.MAIA_ENTITY_NOT_FOUND_EXCEPTION)
        addImportFor(Fqcns.MAIA_ENTITY_CLASS_AND_PK)

        val fieldNamesCsv = fieldNamesCsv(this.entityDef.primaryKeyClassFields)
        val fieldNamesAndTypesCsv = fieldNamesAndTypesCsv(this.entityDef.primaryKeyClassFields)

        blankLine()
        blankLine()
        appendLine("    @Throws(EntityNotFoundException::class)")
        appendLine("    fun findByPrimaryKey($fieldNamesAndTypesCsv): ${entityDef.entityUqcn} {")
        blankLine()
        appendLine("        return findByPrimaryKeyOrNull($fieldNamesCsv)")
        appendLine("            ?: throw EntityNotFoundException(")
        appendLine("                EntityClassAndPk(")
        appendLine("                    ${entityDef.entityUqcn}::class.java,")
        appendLine("                    mapOf(")

        this.entityDef.primaryKeyFields.forEach { field ->
            appendLine("                        \"${field.classFieldName}\" to ${field.classFieldName},")
        }

        appendLine("                    )")
        appendLine("                ),")
        appendLine("                ${entityDef.metaClassDef.uqcn}.TABLE_NAME")
        appendLine("            )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findByPrimaryKeyOrNull`() {

        val fieldNamesAndTypesCsv = this.entityDef.primaryKeyFields.joinToString(", ") {
            "${it.classFieldName}: ${it.fieldType.unqualifiedToString}"
        }

        val whereClauseFields = this.entityDef.primaryKeyFields.joinToString(" and ") {
            "${it.tableColumnName} = :${it.classFieldName}"
        }

        blankLine()
        blankLine()
        appendLine("    fun findByPrimaryKeyOrNull($fieldNamesAndTypesCsv): ${entityDef.entityUqcn}? {")
        blankLine()
        appendLine("        return jdbcOps.queryForList(")
        appendLine("            \"select * from ${this.entityDef.schemaAndTableName} where $whereClauseFields\",")
        appendLine("            SqlParams().apply {")

        this.entityDef.primaryKeyFields.forEach {
            renderSqlParamAddValueFor(it, "            ", entityParameterName = null, 8, { line -> appendLine(line) })
        }

        appendLine("            },")
        appendLine("            this.entityRowMapper")
        appendLine("        ).firstOrNull()")
        blankLine()
        appendLine("    }")


    }


    private fun `render function count`() {

        blankLine()
        blankLine()
        appendLine("    fun count(): Long {")
        blankLine()
        appendLine("        return jdbcOps.queryForLong(")
        appendLine("            \"select count(*) from ${entityDef.schemaAndTableName}\",")
        appendLine("            SqlParams()")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function countWithFilter`() {

        blankLine()
        blankLine()
        appendLine("    fun count(filter: ${this.entityDef.entityFilterClassDef.uqcn}): Long {")
        blankLine()
        appendLine("        val whereClause = filter.whereClause(this.fieldConverter)")
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        filter.populateSqlParams(sqlParams)")
        blankLine()
        appendLine("        return jdbcOps.queryForLong(")
        appendLine("            \"\"\"")
        appendLine("            select count(*) from ${entityDef.schemaAndTableName}")
        appendLine("            where \$whereClause")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            sqlParams")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render findAll`() {

        if (this.entityDef.allowFindAll.value == false) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findAll(): List<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        return this.jdbcOps.queryForList(")
        appendLine("            \"select * from ${entityDef.schemaAndTableName}\",")
        appendLine("            SqlParams(),")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render findAllEffective`() {

        if (this.entityDef.allowFindAll.value == false || (this.entityDef.hasEffectiveTimestamps.value == false && this.entityDef.hasEffectiveLocalDates.value == false)) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findAllEffective(): List<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        return this.jdbcOps.queryForList(\"\"\"")
        appendLine("            select * from ${entityDef.schemaAndTableName}")
        appendLine("            where effective_from <= current_timestamp")
        appendLine("            and (effective_to > current_timestamp or effective_to is null)")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams(),")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllByFilter`() {

        blankLine()
        blankLine()
        appendLine("    fun findAllBy(filter: ${this.entityDef.entityFilterClassDef.uqcn}): List<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        val whereClause = filter.whereClause(this.fieldConverter)")
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        filter.populateSqlParams(sqlParams)")
        blankLine()
        appendLine("        return this.jdbcOps.queryForList(")
        appendLine("            \"select * from ${entityDef.schemaAndTableName} where \$whereClause\",")
        appendLine("            sqlParams,")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllByFilterAsSequence`() {

        if (this.entityDef.allowFindAll.value == false) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findAllByFilterAsSequence(filter: ${this.entityDef.entityFilterClassDef.uqcn}): Sequence<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        val whereClause = filter.whereClause(this.fieldConverter)")
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        filter.populateSqlParams(sqlParams)")
        blankLine()
        appendLine("        return this.jdbcOps.queryForSequence(")
        appendLine("            \"select * from ${entityDef.schemaAndTableName} where \$whereClause\",")
        appendLine("            sqlParams,")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findIdsAsSequence`() {

        if (this.entityDef.allowFindAll.value == false) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findIdsAsSequence(filter: ${this.entityDef.entityFilterClassDef.uqcn}): Sequence<DomainId> {")
        blankLine()
        appendLine("        val whereClause = filter.whereClause(this.fieldConverter)")
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        filter.populateSqlParams(sqlParams)")
        blankLine()
        appendLine("        return this.jdbcOps.queryForSequence(")
        appendLine("            \"select id from ${entityDef.schemaAndTableName} where \$whereClause\",")
        appendLine("            sqlParams,")
        appendLine("            { rsa -> rsa.readDomainId(\"id\") }")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllByFilterAndPageRequest`() {

        addImportFor(Fqcns.SPRING_PAGEABLE)

        blankLine()
        blankLine()
        appendLine("    fun findAllBy(filter: ${this.entityDef.entityFilterClassDef.uqcn}, pageable: Pageable): List<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        val whereClause = filter.whereClause(this.fieldConverter)")
        appendLine("        val orderByClause = orderByClauseFor(pageable)")
        appendLine("        val limitClause = limitClauseFor(pageable)")
        appendLine("        val offsetClause = offsetClauseFor(pageable)")
        blankLine()
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        filter.populateSqlParams(sqlParams)")
        blankLine()
        appendLine("        return this.jdbcOps.queryForList(")
        appendLine("            \"select * from ${entityDef.schemaAndTableName} where \$whereClause \$orderByClause \$limitClause \$offsetClause\",")
        appendLine("            sqlParams,")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    private fun orderByClauseFor(pageable: Pageable): String {")
        blankLine()
        appendLine("        val properties = pageable.sort.map { \"\${it.property} \${it.direction}\" }.joinToString(\", \")")
        appendLine("        return \"ORDER BY \$properties\"")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    private fun limitClauseFor(pageable: Pageable): String {")
        blankLine()
        appendLine("        return \"LIMIT \${pageable.pageSize}\"")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    private fun offsetClauseFor(pageable: Pageable): String {")
        blankLine()
        appendLine("        return \"OFFSET \${pageable.offset}\"")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllAsSequence`() {

        blankLine()
        blankLine()
        appendLine("    fun findAllAsSequence(): Sequence<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        return this.jdbcOps.queryForSequence(")
        appendLine("            \"select * from ${entityDef.schemaAndTableName};\",")
        appendLine("            SqlParams(),")
        appendLine("            this.entityRowMapper,")
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllPrimaryKeysAsSequence`() {

        val primaryKeyColumnsCsv = entityDef.primaryKeyFields.map { it.tableColumnName }.joinToString(", ")

        blankLine()
        blankLine()
        appendLine("    fun findAllPrimaryKeysAsSequence(): Sequence<${entityDef.primaryKeyType}> {")
        blankLine()
        appendLine("        return this.jdbcOps.queryForSequence(")
        appendLine("            \"select $primaryKeyColumnsCsv from ${entityDef.schemaAndTableName};\",")
        appendLine("            SqlParams(),")

        if (entityDef.primaryKeyRowMapperDef == null) {
            appendLine("            { rsa -> rsa.readDomainId(\"id\") }")
        } else {
            appendLine("            this.primaryKeyRowMapper")
        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun `render function deleteByPrimaryKey`() {

        if (this.entityDef.isNotDeletable) {
            return
        }

        addImportFor(Fqcns.MAIA_ENTITY_NOT_FOUND_EXCEPTION)
        addImportFor(Fqcns.MAIA_ENTITY_CLASS_AND_PK)

        val fieldNamesAnded = fieldNamesAnded(this.entityDef.primaryKeyFields.map { it.classFieldDef })
        val fieldNamesCsv = this.entityDef.primaryKeyFields.map { it.classFieldName }.joinToString(", ")
        val fieldNamesAndTypesCsv = this.entityDef.primaryKeyFields.joinToString(", ") { "${it.classFieldName}: ${it.fieldType.unqualifiedToString}" }

        blankLine()
        blankLine()
        appendLine("    fun deleteByPrimaryKey($fieldNamesAndTypesCsv): Boolean {")
        blankLine()
        appendLine("        val existingEntity = findByPrimaryKeyOrNull($fieldNamesCsv)")
        blankLine()
        appendLine("        if (existingEntity == null) {")
        appendLine("            return false")
        appendLine("        }")
        blankLine()
        appendLine("        val deletedCount = this.jdbcOps.update(")
        appendLine("            \"delete from ${entityDef.schemaAndTableName} where ${this.entityDef.primaryKeyFields.joinToString(" and ") { "${it.tableColumnName} = :${it.classFieldName}" }}\",")
        appendLine("            SqlParams().apply {")

        this.entityDef.primaryKeyFields.forEach {
            renderSqlParamAddValueFor(it, "                ", entityParameterName = null, 8, { line -> appendLine(line) })
        }

        appendLine("            }")
        appendLine("        )")

        if (entityDef.withVersionHistory.value) {

            blankLine()
            appendLine("        if (deletedCount > 0) {")
            blankLine()

            if (entityHierarchy.hasSubclasses()) {

                appendLine("            when (existingEntity) {")

                entityHierarchy.concreteEntityDefs.forEach { concreteEntityDef ->

                    appendLine("                is ${concreteEntityDef.entityUqcn} -> insertHistory(existingEntity, existingEntity.version + 1, ChangeType.DELETE)")

                }

                appendLine("            }")
                blankLine()
                appendLine("            return true")
                blankLine()
                appendLine("        } else {")
                blankLine()
                appendLine("            return false")
                blankLine()
                appendLine("        }")

            } else {

                appendLine("            this.historyDao.insert(history(existingEntity, existingEntity.version + 1, ChangeType.DELETE))")
                appendLine("        }")
                blankLine()
                appendLine("        return deletedCount > 0")

            }

        } else {

            blankLine()
            appendLine("        return deletedCount > 0")

        }

        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun removeByPrimaryKey($fieldNamesAndTypesCsv): ${entityDef.entityUqcn}? {")
        blankLine()
        appendLine("        val found = findByPrimaryKeyOrNull($fieldNamesCsv)")
        blankLine()
        appendLine("        if (found != null) {")
        appendLine("            deleteByPrimaryKey($fieldNamesCsv)")
        appendLine("        }")
        blankLine()
        appendLine("        return found")
        blankLine()
        appendLine("    }")

    }


    private fun `render function deleteAll`() {

        if (entityDef.isNotDeletable || entityDef.allowDeleteAll.value == false) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun deleteAll() {")
        appendLine("        this.jdbcOps.update(\"delete from ${entityDef.schemaAndTableName}\")")
        appendLine("    }")

    }


    private fun `render deleteBy for indexes`() {

        this.entityDef.uniqueIndexDefs.forEach { entityIndexDef -> `render function deleteByForIndex`(entityIndexDef.indexDef) }

    }


    private fun `render function deleteByForIndex`(indexDef: IndexDef) {

        if (this.entityDef.isNotDeletable) {
            return
        }

        if (indexDef.isForIdAndVersion) {
            return
        }

        val classFieldDefs = indexDef.entityFieldDefs.map { it.classFieldDef }
        val functionParameters = buildFunctionParameters(classFieldDefs).joinToString(", ") { it }
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)
        val fieldNamesCsv = classFieldDefs.map { it.classFieldName }.joinToString()

        blankLine()
        blankLine()
        appendLine("    fun deleteBy$fieldNamesAnded($functionParameters): Boolean {")
        blankLine()
        appendLine("        val existingEntity = findOneOrNullBy$fieldNamesAnded($fieldNamesCsv)")
        blankLine()
        appendLine("        if (existingEntity != null) {")
        blankLine()
        appendLine("            val deletedCount = this.jdbcOps.update(")
        appendLine("                \"delete from ${entityDef.schemaAndTableName} where id = :id\",")
        appendLine("                SqlParams().apply {")
        appendLine("                    addValue(\"id\", existingEntity.id)")
        appendLine("                }")
        appendLine("            )")

        if (entityDef.withVersionHistory.value) {

            blankLine()
            appendLine("            this.historyDao.insert(history(existingEntity, existingEntity.version + 1, ChangeType.DELETE))")

        }

        blankLine()
        appendLine("            return deletedCount > 0")
        blankLine()
        appendLine("        } else {")
        blankLine()
        appendLine("            return false")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun `render finders for indexes`() {

        this.entityDef.databaseIndexDefs.filter { it.isUnique }.forEach { `render function findOneByForFields`(it.indexDef.entityFieldDefs) }
        this.entityDef.databaseIndexDefs.filter { it.isUnique == false }.forEach { `render function findBy for fields`(it.indexDef.entityFieldDefs) }

        if (this.entityDef.hasEffectiveTimestamps.value || this.entityDef.hasEffectiveLocalDates.value) {
            this.entityDef.databaseIndexDefs.filter { it.isUnique == false }.forEach { `render function findEffectiveBy for fields`(it.indexDef.entityFieldDefs) }
        }

    }


    private fun `render function findOneByForFields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)
        val fieldNamesCsv = classFieldDefs.map { it.classFieldName }.joinToString()
        val columnClauses = buildColumnClauses(entityFieldDefs)

        blankLine()
        blankLine()

        if (entityFieldDefs.size > 1) {

            appendLine("    fun findOneOrNullBy$fieldNamesAnded(")
            renderStrings(methodParameters, indent = 8)
            newLine()
            appendLine("    ): ${entityDef.entityUqcn}? {")

        } else {

            appendLine("    fun findOneOrNullBy${fieldNamesAnded}(${methodParameters.first()}): ${entityDef.entityUqcn}? {")

        }

        blankLine()
        appendLine("        return jdbcOps.queryForList(")
        appendLine("            \"\"\"")
        appendLine("            select * from ${entityDef.schemaAndTableName}")

        appendWhereOrAndClauses(entityFieldDefs)

        if (entityDef.typeDiscriminatorOrNull != null) {
            appendLine("            and type_discriminator = '${entityDef.typeDiscriminator}'")
        }

        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams().apply {")

        entityFieldDefs.forEach { entityFieldDef ->
            renderSqlParamAddValueFor(entityFieldDef, "            ", entityParameterName = null, 8, { line -> appendLine(line) })
        }

        appendLine("            },")
        appendLine("            this.entityRowMapper")
        appendLine("        ).firstOrNull()")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    @Throws(EntityNotFoundException::class)")

        if (entityFieldDefs.size > 1) {

            appendLine("    fun findOneBy${fieldNamesAnded}(")
            renderStrings(methodParameters, indent = 8)
            newLine()
            appendLine("    ): ${entityDef.entityUqcn} {")

        } else {

            appendLine("    fun findOneBy${fieldNamesAnded}(${methodParameters.first()}): ${entityDef.entityUqcn} {")

        }

        blankLine()
        appendLine("        return findOneOrNullBy${fieldNamesAnded}($fieldNamesCsv)")
        appendLine("            ?: throw EntityNotFoundException(\"No record with column $columnClauses found in table ${entityDef.schemaAndTableName}.\", ${entityDef.metaClassDef.uqcn}.TABLE_NAME)")
        blankLine()
        appendLine("    }")

    }


    private fun buildColumnClauses(entityFieldDefs: List<EntityFieldDef>): List<String> {

        return entityFieldDefs.map { "${it.tableColumnName} = \$${it.classFieldName}" }

    }


    private fun `render function findBy for fields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)

        blankLine()
        blankLine()

        if (entityFieldDefs.size > 1) {

            appendLine("    fun findBy${fieldNamesAnded}(")
            renderStrings(methodParameters, indent = 8)
            newLine()
            appendLine("    ): List<${entityDef.entityUqcn}> {")

        } else {

            appendLine("    fun findBy${fieldNamesAnded}(${methodParameters.first()}): List<${entityDef.entityUqcn}> {")

        }

        blankLine()
        appendLine("        return jdbcOps.queryForList(")
        appendLine("            \"\"\"")
        appendLine("            select * from ${this.entityDef.schemaAndTableName}")

        appendWhereOrAndClauses(entityFieldDefs)

        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams().apply {")

        entityFieldDefs.forEach { entityFieldDef ->
            renderSqlParamAddValueFor(entityFieldDef, "            ", entityParameterName = null, 8, { line -> appendLine(line) })
        }

        appendLine("            },")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")


    }


    private fun `render function findEffectiveBy for fields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)

        val returnType = if (this.entityDef.hasSingleEffectiveRecord.value) {
            "${this.entityDef.entityUqcn.value}?"
        } else {
            "List<${this.entityDef.entityUqcn}>"
        }

        val queryFunction = if (this.entityDef.hasSingleEffectiveRecord.value) {
            "queryForObjectOrNull"
        } else {
            "queryForList"
        }

        blankLine()
        blankLine()

        if (entityFieldDefs.size > 1) {

            appendLine("    fun findEffectiveBy${fieldNamesAnded}(")
            renderStrings(methodParameters, indent = 8)
            newLine()
            appendLine("    ): $returnType {")

        } else {

            appendLine("    fun findEffectiveBy${fieldNamesAnded}(${methodParameters.first()}): $returnType {")

        }

        blankLine()
        appendLine("        return jdbcOps.$queryFunction(")
        appendLine("            \"\"\"")
        appendLine("            select * from ${this.entityDef.schemaAndTableName}")

        appendWhereOrAndClauses(entityFieldDefs)
        appendLine("            and effective_from <= current_timestamp")
        appendLine("            and (effective_to > current_timestamp or effective_to is null)")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams().apply {")

        entityFieldDefs.forEach { entityFieldDef ->
            appendLine("                ${sqlParamAddFunctionName(entityFieldDef.fieldType)}(\"${entityFieldDef.classFieldName}\", ${entityFieldDef.classFieldName})")
        }

        appendLine("            },")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
        blankLine()
        appendLine("    }")


    }


    private fun `render existsBy functions`() {

        val uniqueIndexFields: List<List<EntityFieldDef>> = this.entityDef.databaseIndexDefs.filter { it.isUnique }.map { it.indexDef.entityFieldDefs }
        val foreignKeyFields: List<List<EntityFieldDef>> = this.entityDef.allForeignKeyEntityFieldDefs.map { listOf(it) }

        val fields = mutableSetOf<List<EntityFieldDef>>()

        uniqueIndexFields.forEach { fields.add(it) }
        foreignKeyFields.forEach { fields.add(it) }

        fields.forEach { `render function existsByForFields`(it) }

    }


    private fun `render function existsByForFields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)

        blankLine()
        blankLine()

        if (entityFieldDefs.size > 1) {

            appendLine("    fun existsBy${fieldNamesAnded}(")
            renderStrings(methodParameters, indent = 8)
            newLine()
            appendLine("    ): Boolean {")

        } else {

            appendLine("    fun existsBy$fieldNamesAnded(${methodParameters.first()}): Boolean {")

        }

        blankLine()
        appendLine("        val count = jdbcOps.queryForInt(")
        appendLine("            \"\"\"")
        appendLine("            select count(*) from ${this.entityDef.schemaAndTableName}")

        appendWhereOrAndClauses(entityFieldDefs)

        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams().apply {")

        entityFieldDefs.forEach { entityFieldDef ->
            renderSqlParamAddValueFor(entityFieldDef, "            ", entityParameterName = null, 8, { line -> appendLine(line) })
        }

        appendLine("            }")
        appendLine("        )")
        blankLine()
        appendLine("        return count > 0")
        blankLine()
        appendLine("    }")

    }


    private fun appendWhereOrAndClauses(entityFieldDefs: List<EntityFieldDef>) {

        entityFieldDefs.forEachIndexed { index, entityFieldDef ->
            val whereOrAnd = if (index == 0) "where" else "and"
            appendLine("            $whereOrAnd ${entityFieldDef.tableColumnName} = :${entityFieldDef.classFieldName}")
        }

    }


    private fun `render upserts for indexes`() {

        if (this.entityDef.isHistoryEntity) {
            return
        }

        this.entityDef.databaseIndexDefs
            .filter { it.isUnique }
            .forEach { this.renderUpsertForUniqueFields(it.indexDef.entityFieldDefs) }

    }


    private fun renderUpsertForUniqueFields(entityFieldDefs: List<EntityFieldDef>) {

        if (this.entityDef.isModifiable) {
            `render upsert by unique fields if entity is modifiable`(entityFieldDefs)
        } else {
            `render upsert by unique fields if entity is unmodifiable`(entityFieldDefs)
        }

    }


    private fun `render upsert by unique fields if entity is modifiable`(entityFieldDefs: List<EntityFieldDef>) {

        addImportFor(Fqcns.JDBC_PREPARED_STATEMENT)
        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)

        val uniqueFieldColumnNames = entityFieldDefs.map { it.tableColumnName }
        val uniqueFieldNamesAnded = fieldNamesAnded(entityFieldDefs.map { it.classFieldDef })

        blankLine()
        blankLine()
        appendLine("    fun upsertBy${uniqueFieldNamesAnded}(upsertEntity: ${this.entityDef.entityUqcn}): ${entityDef.entityUqcn} {")
        blankLine()
        appendLine("        val persistedEntity = jdbcOps.execute(")
        appendLine("            \"\"\"")
        appendLine("            insert into ${entityDef.schemaAndTableName} (")

        val fieldCollectionNames = entityDef.allEntityFieldsSorted.map { entityFieldDef -> entityFieldDef.tableColumnName.value }
        renderStrings(fieldCollectionNames, indent = 16)
        newLine()

        appendLine("            ) values (")

        val fieldNames = entityDef.allEntityFieldsSorted.map { entityFieldDef -> ":" + entityFieldDef.classFieldName.value }
        renderStrings(fieldNames, indent = 16)
        newLine()

        appendLine("            )")
        appendLine("            on conflict (${uniqueFieldColumnNames.joinToString(", ")})")
        appendLine("            do update set")

        val modifiableFields = entityDef.allModifiableFieldDef.sorted().map { "${it.tableColumnName} = :${it.classFieldName}" }

        renderStrings(modifiableFields, indent = 16)

        if (entityDef.versioned.value) {
            append(",")
            newLine()
            appendLine("                v = ${entityDef.schemaAndTableName}.v + 1")
        } else {
            newLine()
        }

        appendLine("            returning *;")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams().apply {")

        val indentSize = 12

        renderSqlParamsAddValueLinesFor("upsertEntity", entityDef, indentSize)

        appendLine("            },")
        appendLine("            { ps: PreparedStatement ->")
        appendLine("                val rs = ps.executeQuery()")
        appendLine("                rs.next()")
        appendLine("                entityRowMapper.mapRow(ResultSetAdapter(rs))")
        appendLine("            }")

        appendLine("        )")

        if (entityDef.withVersionHistory.value) {
            blankLine()
            appendLine("        val changeType = if (persistedEntity!!.id != upsertEntity.id) ChangeType.UPDATE else ChangeType.CREATE")

            if (entityHierarchy.hasSubclasses()) {

                blankLine()
                appendLine("        when (persistedEntity) {")

                entityHierarchy.concreteEntityDefs.forEach { concreteEntityDef ->

                    appendLine("            is ${concreteEntityDef.entityUqcn} -> insertHistory(persistedEntity, persistedEntity.version, changeType)")

                }

                appendLine("        }")

            } else {

                appendLine("        insertHistory(persistedEntity, persistedEntity.version, changeType)")

            }

        }

        blankLine()
        appendLine("        return persistedEntity!!")
        blankLine()
        appendLine("    }")

    }


    private fun `render upsert by unique fields if entity is unmodifiable`(entityFieldDefs: List<EntityFieldDef>) {

        addImportFor(Fqcns.JDBC_PREPARED_STATEMENT)
        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)
        addImportFor(Fqcns.MAIA_JDBC_ROW_MAPPER)

        val uniqueFieldColumnNames = entityFieldDefs.map { it.tableColumnName }
        val uniqueFieldNamesAnded = fieldNamesAnded(entityFieldDefs.map { it.classFieldDef })

        blankLine()
        blankLine()
        appendLine("    fun upsertBy${uniqueFieldNamesAnded}(upsertEntity: ${this.entityDef.entityUqcn}): DomainId {")
        blankLine()
        appendLine("        return jdbcOps.execute(")
        appendLine("            \"\"\"")
        appendLine("            with input_rows(")

        val fieldCollectionNames = entityDef.allEntityFieldsSorted.map { entityFieldDef -> entityFieldDef.tableColumnName.value }
        renderStrings(fieldCollectionNames, indent = 16)
        newLine()

        appendLine("            ) as (")
        appendLine("                values (")

        val fieldNames = entityDef.allEntityFieldsSorted.map { entityFieldDef -> "cast(:" + entityFieldDef.classFieldName.value + " as ${FieldTypeRendererHelper.determineSqlDataType(entityFieldDef.fieldType)})" }
        renderStrings(fieldNames, indent = 20)
        newLine()

        appendLine("                )")
        appendLine("            )")
        appendLine("            , ins as (")
        appendLine("                insert into ${entityDef.schemaAndTableName} (")

        renderStrings(fieldCollectionNames, indent = 20)
        newLine()

        appendLine("                )")
        appendLine("                select * from input_rows")
        appendLine("                on conflict (${uniqueFieldColumnNames.joinToString(", ")}) do nothing")
        appendLine("                returning id")
        appendLine("            )")
        appendLine("            select 'i' as source, id")
        appendLine("            from ins")
        appendLine("            union all")
        appendLine("            select 's' as source, c.id")
        appendLine("            from input_rows")
        appendLine("            join ${entityDef.schemaAndTableName} c using (${uniqueFieldColumnNames.joinToString(", ")});")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams().apply {")

        val indentSize = 12

        renderSqlParamsAddValueLinesFor("upsertEntity", entityDef, indentSize)

        appendLine("            },")
        appendLine("            { ps: PreparedStatement ->")
        appendLine("                val rs = ps.executeQuery()")
        appendLine("                rs.next()")
        appendLine("                idRowMapper.mapRow(ResultSetAdapter(rs))")
        appendLine("            }")
        appendLine("        )!!")
        blankLine()
        appendLine("    }")

    }


    private fun `render function fetchForEdit`() {

        if (this.entityDef.isConcrete == false || this.entityDef.crudDef.crudApiDefs.updateApiDef == null) {
            return
        }

        val selectColumns = entityDef.allEntityFieldsSorted.flatMap { entityFieldDef ->

            val foreignKeyFieldDef = entityFieldDef.foreignKeyFieldDef

//            if (
//                foreignKeyFieldDef == null
//                || (entityFieldDef.classFieldDef.isModifiableBySystem == false && entityFieldDef.classFieldDef.isEditableByUser.value == false)
//            ) {

            if (foreignKeyFieldDef == null) {

                listOf("${entityDef.tableName}.${entityFieldDef.tableColumnName} as ${entityFieldDef.classFieldName}")

            } else {

                val foreignKeyEntity = foreignKeyFieldDef.foreignEntityDef
                val foreignTableIsReferencedMoreThanOnce = (foreignKeyTableCount[foreignKeyEntity.schemaAndTableName] ?: 0) > 1


                val tableOrAlias = if (foreignTableIsReferencedMoreThanOnce) {
                    "${foreignKeyFieldDef.foreignKeyFieldName.toSnakeCase()}_${foreignKeyEntity.tableName}"
                } else {
                    foreignKeyEntity.schemaAndTableName
                }

                val idAndNameDef = foreignKeyIdAndNameDef(foreignKeyEntity, entityFieldDef)

                listOf(
                    "$tableOrAlias.id as ${foreignKeyFieldDef.foreignKeyFieldName}Id",
                    "$tableOrAlias.${idAndNameDef.nameEntityFieldDef.tableColumnName} as ${foreignKeyFieldDef.foreignKeyFieldName}Name"
                )

            }

        }

        val joinClauses = entityDef.allForeignKeyEntityFieldDefs.map { entityFieldDef ->

            val foreignKeyFieldDef = entityFieldDef.foreignKeyFieldDef!!
            val foreignEntityDef = foreignKeyFieldDef.foreignEntityDef

            val foreignTableIsReferencedMoreThanOnce = (foreignKeyTableCount[foreignEntityDef.schemaAndTableName] ?: 0) > 1

            if (foreignTableIsReferencedMoreThanOnce) {

                val alias = "${foreignKeyFieldDef.foreignKeyFieldName.toSnakeCase()}_${foreignEntityDef.tableName}"

                """join ${foreignEntityDef.schemaAndTableName} $alias
                on $alias.id = ${entityDef.tableName}.${entityFieldDef.tableColumnName}
                """.trimIndent()

            } else {

                """join ${foreignEntityDef.schemaAndTableName}
                on ${foreignEntityDef.schemaAndTableName}.id = ${entityDef.tableName}.${entityFieldDef.tableColumnName}
                """.trimIndent()

            }

        }

        addImportFor(entityDef.fetchForEditDtoFqcn)

        blankLine()
        blankLine()
        appendLine("    fun fetchForEdit($primaryKeyFieldNamesAndTypesCsv): ${entityDef.fetchForEditDtoFqcn.uqcn} {")
        blankLine()
        appendLine("        return this.jdbcOps.queryForList(")
        appendLine("            \"\"\"")
        appendLine("            select")
        renderStrings(selectColumns, indent = 16)
        newLine()
        appendLine("            from ${entityDef.schemaAndTableName}")
        joinClauses.forEach { appendLine("            $it") }

        val primaryKeyClauses = entityDef.primaryKeyFields.joinToString(" and ") { entityFieldDef ->
            "${entityDef.tableName}.${entityFieldDef.tableColumnName} = :${entityFieldDef.classFieldName}"
        }

        appendLine("            where $primaryKeyClauses")
        appendLine("            \"\"\",")
        appendLine("            SqlParams().apply {")
        appendLine("                addValue(\"id\", id)")
        appendLine("            },")
        appendLine("            this.fetchForEditDtoRowMapper")
        appendLine("        ).firstOrNull()")
        appendLine("            ?: throw EntityNotFoundException(EntityClassAndPk(${entityDef.entityUqcn}::class.java, $mapOfPrimaryKeyFields), ${entityDef.metaClassDef.uqcn}.TABLE_NAME)")
        blankLine()
        appendLine("    }")

    }


    private fun foreignKeyIdAndNameDef(
        foreignKeyEntity: EntityDef,
        entityFieldDef: EntityFieldDef
    ): EntityIdAndNameDef {

        try {
            return foreignKeyEntity.entityIdAndNameDef
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Foreign key field $entityFieldDef references an Entity that does not have an idAndNameDef", e)
        }

    }


    private fun `render function setFields`() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        addImportFor(Fqcns.MAIA_FIELD_UPDATE)

        blankLine()
        blankLine()
        appendLine("    fun setFields(updaters: List<${entityDef.entityUpdaterClassDef.uqcn}>) {")
        blankLine()
        appendLine("        updaters.forEach { setFields(it) }")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun setFields(updater: ${entityDef.entityUpdaterClassDef.uqcn}): Int {")
        blankLine()
        appendLine("        val sql = StringBuilder()")
        appendLine("        val sqlParams = SqlParams()")
        blankLine()
        appendLine("        sql.append(\"update ${entityDef.schemaAndTableName} set \")")
        blankLine()
        appendLine("        val fieldClauses = updater.fields")

        if (entityDef.versioned.value) {
            appendLine("            .plus(FieldUpdate(\"v_incremented\", \"v\", updater.version + 1))")
        }

        appendLine("            .map { field ->")
        blankLine()
        appendLine("                addField(field, sqlParams)")
        appendLine("                \"\${field.dbColumnName} = :\${field.classFieldName}\"")
        blankLine()
        appendLine("            }.joinToString(\", \")")
        blankLine()
        appendLine("        sql.append(fieldClauses)")
        appendLine("        sql.append(\" where ${this.entityDef.primaryKeyFields.joinToString(" and ") { "${it.tableColumnName} = :${it.classFieldName}" }}\")")

        if (entityDef.versioned.value) {
            appendLine("        sql.append(\" and v = :v\")")
        }

        blankLine()

        entityDef.primaryKeyFields.forEach {
            append("        sqlParams.")
            renderSqlParamAddValueFor(it, "", entityParameterName = "updater", 0, { line -> appendLine(line) })
        }

        if (entityDef.versioned.value) {
            appendLine("        sqlParams.addValue(\"v\", updater.version)")
            appendLine("        sqlParams.addValue(\"v_incremented\", updater.version + 1)")
        }

        blankLine()

        if (entityDef.withVersionHistory.value) {

            addImportFor(Fqcns.MAIA_JDBC_OPTIMISTIC_LOCKING_EXCEPTION)

            appendLine("        val updateCount = this.jdbcOps.update(sql.toString(), sqlParams)")
            blankLine()
            appendLine("        if (updateCount == 0) {")
            blankLine()
            appendLine("            throw OptimisticLockingException(${entityDef.metaClassDef.uqcn}.TABLE_NAME, updater.primaryKey, updater.version)")
            blankLine()
            appendLine("        } else {")
            blankLine()

            val updaterPrimaryKeyFieldsCsv = this.entityDef.primaryKeyClassFields.joinToString(", ") { "updater.${it.classFieldName}" }

            appendLine("            val updatedEntity = findByPrimaryKey($updaterPrimaryKeyFieldsCsv)")

            if (entityHierarchy.hasSubclasses()) {

                blankLine()
                appendLine("            when (updatedEntity) {")

                entityHierarchy.concreteEntityDefs.forEach { concreteEntityDef ->

                    appendLine("                is ${concreteEntityDef.entityUqcn} -> insertHistory(updatedEntity, ChangeType.UPDATE)")

                }

                appendLine("            }")

            } else {

                appendLine("            insertHistory(updatedEntity, ChangeType.UPDATE)")

            }

            blankLine()
            appendLine("        }")
            blankLine()
            appendLine("        return updateCount")

        } else {

            appendLine("        return this.jdbcOps.update(sql.toString(), sqlParams)")

        }

        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    private fun addField(field: FieldUpdate, sqlParams: SqlParams) {")
        blankLine()
        appendLine("        when (field.classFieldName) {")

        this.entityDef.allEntityFieldsSorted.filter { it.classFieldDef.isModifiableBySystem || it.classFieldDef.isEditableByUser.value }.forEach { entityFieldDef ->

            val classFieldDef = entityFieldDef.classFieldDef
            val fieldType = classFieldDef.fieldType

            addImportFor(fieldType)

            val classFieldName = entityFieldDef.classFieldName
            val sqlParamsAddFunc = sqlParamAddFunctionName(fieldType)
            val sqlParamsMapperFunc = sqlParamMapperFunction(fieldType)

            val fieldValueAs = "field.value as ${classFieldDef.unqualifiedToString}"

            val fieldValueParameter = if (classFieldDef.isMap) {
                "this.objectMapper.writeValueAsString($fieldValueAs)"
            } else if (classFieldDef.isValueClass) {
                "($fieldValueAs)?.value"
            } else {
                fieldValueAs
            }

            appendLine("            \"${classFieldName}\" -> sqlParams.$sqlParamsAddFunc(\"$classFieldName\", $fieldValueParameter)$sqlParamsMapperFunc")

        }

        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun renderSqlParamsAddValueLinesFor(
        entityParameterName: String,
        entityDef: EntityDef,
        indentSize: Int
    ) {

        val indent = "".padEnd(indentSize, ' ')

        entityDef.allEntityFieldsSorted.forEach { entityFieldDef ->

            renderSqlParamAddValueFor(entityFieldDef, indent, entityParameterName, indentSize, { line -> appendLine(line) })

        }

    }


    private fun EntityDef.databaseColumnNames(): List<String> = allEntityFieldsSorted
        .filterNot { it.isDerived.value }
        .map { it.tableColumnName.value }


}
