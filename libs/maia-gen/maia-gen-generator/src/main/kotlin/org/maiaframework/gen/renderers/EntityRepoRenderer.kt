package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.IndexDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import java.time.Instant

class EntityRepoRenderer(private val entityHierarchy: EntityHierarchy) : AbstractKotlinRenderer(entityHierarchy.entityDef.entityRepoClassDef) {


    private val entityDef = entityHierarchy.entityDef


    private val cacheable = entityDef.cacheableDef != null


    init {

        addConstructorArg(aClassField("dao", entityDef.entityDaoFqcn).privat().build())

        if (cacheable) {
            addConstructorArg(aClassField("hazelcastInstance", Fqcns.HAZELCAST_INSTANCE).privat().build())
        }

    }


    override fun renderPreClassFields() {

        addImportRaw("org.maiaframework.common.logging.getLogger")

        blankLine()
        blankLine()
        appendLine("    private val logger = getLogger<${classDef.uqcn}>()")

        if (cacheable) {
            addImportFor(Fqcns.HAZELCAST_IMAP)
            blankLine()
            blankLine()
            appendLine("    private val cache: IMap<DomainId, ${entityDef.entityUqcn}> = this.hazelcastInstance.getMap(\"${entityDef.entityCacheName}\")")
        }

    }


    override fun renderFunctions() {

        `render function findByIdOrNull`()
        `render function findById`()
        `render function findAllAsSequence`()
        `render function findIdsAsSequence`()
        `render function findAllIdsAsSequence`()
        `render function findAllEffective`()
        `render function findAllByFilter`()
        `render function findAllByFilterAsSequence`()
        `render finders for indexes`()
        `render existsBy functions`()
        `render function insert`()
        `render function bulkInsert`()
        `render function bulkInsertOfCsvRecords`()
        `render function setFields`()
        `render upserts for indexes`()
        `render function deleteById`()
        `render function deleteAll`()
        `render deleteBy for indexes`()
        `render function removeById`()
        `render function idAndNameFor`()

    }


    private fun `render function findById`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        if (cacheable) {

            append("""
            |
            |
            |    fun findById(id: DomainId): ${entityHierarchy.entityDef.entityUqcn} {
            |
            |        return cache[id]
            |            ?: dao.findById(id).also {
            |                cache[id] = it
            |            }
            |
            |    }
            |""".trimMargin())

        } else {

            append("""
            |
            |
            |    fun findById(id: DomainId): ${entityHierarchy.entityDef.entityUqcn} {
            |
            |        return dao.findById(id)
            |
            |    }
            |""".trimMargin())

        }


    }


    private fun `render function findByIdOrNull`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        if (cacheable) {

            append("""
            |
            |
            |    fun findByIdOrNull(id: DomainId): ${entityHierarchy.entityDef.entityUqcn}? {
            |
            |        return cache[id]
            |            ?: dao.findByIdOrNull(id).also { entity ->
            |                entity?.let { cache[id] = it }
            |            }
            |
            |    }
            |""".trimMargin())

        } else {

            append("""
            |
            |
            |    fun findByIdOrNull(id: DomainId): ${entityHierarchy.entityDef.entityUqcn}? {
            |
            |        return dao.findByIdOrNull(id)
            |
            |    }
            |""".trimMargin())

        }


    }


    private fun `render function findAllAsSequence`() {

        append("""
            |
            |
            |    fun findAllAsSequence(): Sequence<${entityDef.entityUqcn}> {
            |    
            |        return this.dao.findAllAsSequence()
            |        
            |    }
            |""".trimMargin())

    }


    private fun `render function findIdsAsSequence`() {

        if (this.entityDef.allowFindAll.value == false) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findIdsAsSequence(filter: ${this.entityDef.entityFilterClassDef.uqcn}): Sequence<DomainId> {")
        blankLine()
        appendLine("        return dao.findIdsAsSequence(filter)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllIdsAsSequence`() {

        blankLine()
        blankLine()
        appendLine("    fun findAllIdsAsSequence(): Sequence<DomainId> {")
        blankLine()
        appendLine("        return dao.findAllIdsAsSequence()")
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


    private fun `render function findBy for fields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNames = classFieldDefs.map { it.classFieldName }.joinToString(", ")
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
        appendLine("        return dao.findBy${fieldNamesAnded}($fieldNames)")
        blankLine()
        appendLine("    }")


    }



    private fun `render function findOneByForFields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)
        val fieldNamesCsv = classFieldDefs.map { it.classFieldName }.joinToString(", ")

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
        appendLine("        return dao.findOneOrNullBy${fieldNamesAnded}($fieldNamesCsv)")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()

        if (entityFieldDefs.size > 1) {

            appendLine("    fun findOneBy${fieldNamesAnded}(")
            renderStrings(methodParameters, indent = 8)
            newLine()
            appendLine("    ): ${entityDef.entityUqcn} {")

        } else {

            appendLine("    fun findOneBy${fieldNamesAnded}(${methodParameters.first()}): ${entityDef.entityUqcn} {")

        }

        blankLine()
        appendLine("        return dao.findOneBy${fieldNamesAnded}($fieldNamesCsv)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findEffectiveBy for fields`(entityFieldDefs: List<EntityFieldDef>) {

        val classFieldDefs = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParameters(classFieldDefs)
        val fieldNamesCsv = classFieldDefs.map { it.classFieldName }.joinToString(", ")
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)

        val returnType = if (this.entityDef.hasSingleEffectiveRecord.value) {
            "${this.entityDef.entityUqcn.value}?"
        } else {
            "List<${this.entityDef.entityUqcn}>"
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
        appendLine("        return dao.findEffectiveBy${fieldNamesAnded}(${fieldNamesCsv})")
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
        val fieldNames = classFieldDefs.map { it.classFieldName }.joinToString(", ")
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
        appendLine("        return dao.existsBy${fieldNamesAnded}($fieldNames)")
        blankLine()
        appendLine("    }")

    }



    private fun `render function insert`() {

        append("""
            |
            |
            |    fun insert(entity: ${this.entityDef.entityUqcn}) {
            |
            |        logger.debug("insert ${"$"}entity")
            |
            |        this.dao.insert(entity)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function bulkInsert`() {

        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        blankLine()
        blankLine()
        appendLine("    fun bulkInsert(entities: List<${entityDef.entityUqcn}>) {")
        blankLine()
        appendLine("        this.dao.bulkInsert(entities)")
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
        appendLine("        dao.bulkInsertOfCsvRecords(csvPersistableRecords)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllEffective`() {

        if (this.entityDef.allowFindAll.value == false || (this.entityDef.hasEffectiveTimestamps.value == false && this.entityDef.hasEffectiveLocalDates.value == false)) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findAllEffective(): List<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        return this.dao.findAllEffective()")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllByFilter`() {

        blankLine()
        blankLine()
        appendLine("    fun findAllBy(filter: ${this.entityDef.entityFilterClassDef.uqcn}): List<${entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        return this.dao.findAllBy(filter)")
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
        appendLine("        return dao.findAllByFilterAsSequence(filter)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function setFields`() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        append("""
            |
            |
            |    fun setFields(updaters: List<${entityDef.entityUpdaterClassDef.uqcn}>) {
            |
            |        logger.debug("setFields ${"$"}updaters")
            |
            |        updaters.forEach { setFields(it) }
            |
            |    }
            |""".trimMargin()
        )

        if (cacheable) {

            appendLine("""
                |
                |
                |    fun setFields(updater: ${this.entityDef.entityUpdaterClassDef.uqcn}): Int {
                |
                |        logger.debug("setFields ${"$"}updater")
                |
                |        val updatedCount = this.dao.setFields(updater)
                |
                |        if (updatedCount > 0) {
                |            this.cache.evict(updater.id)
                |        }
                |
                |        return updatedCount
                |
                |    }""".trimMargin()
            )

        } else {

            appendLine("""
                |
                |
                |    fun setFields(updater: ${this.entityDef.entityUpdaterClassDef.uqcn}): Int {
                |
                |        logger.debug("setFields ${"$"}updater")
                |
                |        return this.dao.setFields(updater)
                |
                |    }""".trimMargin()
            )

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
            `render upsert for unique fields for modifiable entity`(entityFieldDefs)
        } else {
            `render upsert for unique fields for unmodifiable entity`(entityFieldDefs)
        }


    }
    private fun `render upsert for unique fields for modifiable entity`(entityFieldDefs: List<EntityFieldDef>) {

        val uniqueFieldNamesAnded = fieldNamesAnded(entityFieldDefs.map { it.classFieldDef })

        blankLine()
        blankLine()
        appendLine("    fun upsertBy${uniqueFieldNamesAnded}(upsertEntity: ${this.entityDef.entityUqcn}): ${entityDef.entityUqcn} {")
        blankLine()
        appendLine("        logger.debug(\"upsert \$upsertEntity\")")
        blankLine()

        if (cacheable) {
            appendLine("        val upsertedEntity = dao.upsertBy${uniqueFieldNamesAnded}(upsertEntity)")
            appendLine("        this.cache.evict(upsertedEntity.id)")
            appendLine("        return upsertedEntity")
        } else {
            appendLine("        return dao.upsertBy${uniqueFieldNamesAnded}(upsertEntity)")
        }

        blankLine()
        appendLine("    }")

    }


    private fun `render upsert for unique fields for unmodifiable entity`(entityFieldDefs: List<EntityFieldDef>) {

        val uniqueFieldNamesAnded = fieldNamesAnded(entityFieldDefs.map { it.classFieldDef })

        blankLine()
        blankLine()
        appendLine("    fun upsertBy${uniqueFieldNamesAnded}(upsertEntity: ${this.entityDef.entityUqcn}): DomainId {")
        blankLine()
        appendLine("        logger.debug(\"upsert \$upsertEntity\")")
        blankLine()

        if (cacheable) {
            appendLine("        val id = dao.upsertBy${uniqueFieldNamesAnded}(upsertEntity)")
            appendLine("        this.cache.evict(id)")
            appendLine("        return id")
        } else {
            appendLine("        return dao.upsertBy${uniqueFieldNamesAnded}(upsertEntity)")
        }

        blankLine()
        appendLine("    }")

    }


    private fun `render function deleteById`() {

        if (this.entityDef.isNotDeletable) {
            return
        }

        if (cacheable) {
            append("""
            |
            |
            |    fun deleteById(id: DomainId) {
            |
            |        this.dao.deleteById(id)
            |        this.cache.evict(id)
            |
            |    }
            |""".trimMargin())
        } else {
            append("""
            |
            |
            |    fun deleteById(id: DomainId) {
            |
            |        this.dao.deleteById(id)
            |
            |    }
            |""".trimMargin())

        }

    }


    private fun `render function deleteAll`() {

        if (entityDef.isNotDeletable || entityDef.allowDeleteAll.value == false) {
            return
        }

        appendLine("""
            |
            |
            |    fun deleteAll() {
            |    
            |       this.dao.deleteAll()
            |       
            |    }""".trimMargin())

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

        appendLine("""
            |
            |
            |    fun deleteBy$fieldNamesAnded($functionParameters): Boolean {
            |
            |        return dao.deleteBy$fieldNamesAnded($fieldNamesCsv)
            |
            |    }""".trimMargin())

    }



    private fun `render function removeById`() {

        if (this.entityDef.isNotDeletable) {
            return
        }

        append("""
            |
            |
            |    fun removeById(id: DomainId): ${this.entityDef.entityUqcn}? {
            |
            |        val found = findByIdOrNull(id)
            |       
            |        if (found != null) {
            |            deleteById(id)
            |        }
            |       
            |        return found
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function idAndNameFor`() {

        if (this.entityDef.hasIdAndNameDtoDef == false) {
            return
        }

        val entityIdAndNameDef = entityDef.entityIdAndNameDef
        addImportFor(entityIdAndNameDef.idAndNameDtoFqcn)

        append("""
            |
            |
            |    fun idAndNameFor(id: DomainId): ${entityIdAndNameDef.dtoUqcn} {
            |
            |        val entity = findById(id)
            |        return ${entityIdAndNameDef.dtoUqcn}(
            |            entity.id,
            |            entity.${entityIdAndNameDef.nameEntityFieldDef.classFieldName}
            |        )
            |
            |    }
            |""".trimMargin())

    }


}
