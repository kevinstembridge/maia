package org.maiaframework.gen.renderers

import org.maiaframework.domain.types.CollectionName
import org.maiaframework.gen.renderers.EntityRendererHelper.renderCallToEntityConstructor
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.IndexDef
import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.mongo.CollectionFieldName
import org.springframework.data.domain.Sort
import java.time.Period
import java.time.ZoneId
import java.util.Date

class MongoDaoRenderer(private val entityHierarchy: EntityHierarchy) : AbstractKotlinRenderer(entityHierarchy.entityDef.daoClassDefToRender) {

    private val entityDef = entityHierarchy.entityDef


    init {

        addConstructorArg(ClassFieldDef.aClassField("mongoClientFacade", Fqcns.MONGO_CLIENT_FACADE).build())
        addConstructorArg(ClassFieldDef.aClassField("fieldConverter", entityDef.entityFieldConverterClassDef.fqcn).build())

        if (this.entityDef.withVersionHistory.value) {
            addConstructorArg(ClassFieldDef.aClassField("historyDao", this.entityDef.historyEntityDef?.daoClassDefToRender!!.fqcn).build())
        }

        if (this.entityDef.databaseIndexDefs.isNotEmpty()) {
            addConstructorArg(ClassFieldDef.aClassField("indexCreator", this.entityDef.daoIndexCreatorClassDef.fqcn).build())
        }

        entityHierarchy.allFieldDefs.forEach { fieldDef ->

            fieldDef.fieldReaderClassField?.let { addConstructorArg(it) }
            fieldDef.fieldWriterClassField?.let { addConstructorArg(it) }

        }

        addImportRaw("org.maiaframework.domain.mongo.toObjectId")

    }


    override fun renderCallToSuperConstructor(superclassDef: ClassDef) {

        addImportFor(CollectionName::class.java)

        if (this.entityDef.historyEntityDef == null) {
            append("(")
            newLine()
            appendLine("        CollectionName(\"${this.entityDef.tableName}\"),")
            appendLine("        mongoClientFacade)")
        } else {
            append("(")
            newLine()
            appendLine("        CollectionName(\"${this.entityDef.tableName}\"),")
            appendLine("        mongoClientFacade,")
            appendLine("        historyDao)")
        }

    }


    override fun renderAdditionalConstructorBody() {

        this.entityDef.databaseIndexDefs.forEach { entityIndexDef -> appendLine("        createIndex_${entityIndexDef.indexDef.indexName}()") }
        blankLine()

    }


    override fun renderInitBlock() {

        val initBlockIsNeeded = this.entityDef.databaseIndexDefs.isNotEmpty() || this.entityDef.isCappedCollection.value

        if (initBlockIsNeeded) {

            blankLine()
            blankLine()
            appendLine("    init {")

        }

        if (this.entityDef.databaseIndexDefs.isNotEmpty()) {

            blankLine()
            appendLine("        this.indexCreator.createIndexes()")

        }

        if (this.entityDef.isCappedCollection.value) {

            blankLine()
            appendLine("        this.mongoClientFacade.ensureCollectionIsCapped(this.collectionName, sizeInBytes = ${this.entityDef.cappedSizeInBytes}L)")

        }

        if (initBlockIsNeeded) {

            blankLine()
            appendLine("    }")

        }

    }

    override fun renderFunctions() {

        renderFunction_getTypeDiscriminator()
        renderFunction_history()
        renderFunction_toDocumentFrom()
        renderFunction_toUpsertDocumentFrom()
        renderFunction_toEntityFrom()
        renderFindersForIndexes()
        renderUpsertsForIndexes()
        renderExistsByForIndexes()
        renderDeleteByForIndexes()
        renderCountWithFilter()
        renderFunction_findAllBy()
        renderFunction_findAllWithPageable()
        renderFunction_findAllByWithPageable()
        renderFunction_setFields()
        renderFunction_setFieldsInBulk()
        renderFunction_convertClassFieldNameToCollectionFieldName()
        renderExistsByForeignKeyFields()
        renderFunction_deleteAll()

    }


    private fun renderFunction_history() {

        val historyEntityDef = this.entityDef.historyEntityDef
                ?: return

        addImportFor(Fqcns.MAIA_CHANGE_TYPE)

        blankLine()
        blankLine()
        appendLine("    override fun history(entity: ${this.entityDef.entityUqcn}, v: Long, changeType: ChangeType): ${historyEntityDef.entityUqcn} {")
        blankLine()


        if (this.entityHierarchy.hasSubclasses()) {

            appendLine("        return when(entity) {")

            for (entity in this.entityHierarchy.concreteEntityDefs) {

                addImportFor(entity.entityClassDef.fqcn)
                appendLine("            is ${entity.entityUqcn} -> ${entity.historyEntityDef!!.entityUqcn.firstToLower()}(entity, changeType)")

            }

            appendLine("            else -> throw IllegalStateException(\"Unknown type in class hierarchy: \" + entity::class.java)")
            appendLine("        }")

        } else {

            val ent = this.entityDef

            renderHistoryEntityConstruction(ent, historyEntityDef)

        }

        blankLine()
        appendLine("    }")

        if (this.entityHierarchy.hasSubclasses()) {
            this.entityHierarchy.concreteEntityDefs.forEach { this.renderHistoryForEntity(it) }
        }

    }


    private fun renderHistoryEntityConstruction(
        ent: EntityDef,
        historyEntityDef: EntityDef
    ) {

        appendLine("        val id = ${ent.entityUqcn}.newId()")
        appendLine("        val entityId = entity.id")

        ent.allClassFieldsSorted.filterNot { it.classFieldName == ClassFieldName.id }
            .forEach { fd ->
                appendLine("        val ${fd.classFieldName} = entity.${fd.classFieldName}")
            }

        EntityRendererHelper.renderCallToEntityConstructor(historyEntityDef, 8, this)

    }


    private fun renderHistoryForEntity(entity: EntityDef) {

        val historyEntityDef = entity.historyEntityDef!!
        addImportFor(historyEntityDef.entityClassDef.fqcn)

        blankLine()
        blankLine()
        appendLine("    private fun ${historyEntityDef.entityUqcn.firstToLower()}(entity: ${entity.entityUqcn}, changeType: ChangeType): ${historyEntityDef.entityUqcn} {")
        blankLine()
        renderHistoryEntityConstruction(entity, historyEntityDef)
        blankLine()
        appendLine("    }")

    }


    private fun renderFindersForIndexes() {

        this.entityDef.databaseIndexDefs.filter { it.isUnique }.forEach { this.renderFunction_findOneByForFields(it.indexDef.entityFieldDefs) }
        this.entityDef.databaseIndexDefs.filterNot { it.isUnique }.forEach { this.renderFinderForIndex(it.indexDef) }

    }


    private fun renderExistsByForIndexes() {

        this.entityDef.uniqueIndexDefs.forEach { entityIndexDef -> renderExistsByForFields { entityIndexDef.indexDef.entityFieldDefs } }

    }


    private fun renderDeleteByForIndexes() {

        this.entityDef.uniqueIndexDefs.forEach { entityIndexDef -> renderDeleteByForIndex(entityIndexDef.indexDef) }

    }


    private fun renderFinderForIndex(indexDef: IndexDef) {

        val methodParameters = buildFunctionParametersFrom(indexDef.classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(indexDef.classFieldDefs)

        blankLine()
        blankLine()
        appendLine("    fun findBy$fieldNamesAnded($methodParameters): List<${this.entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        val query = Document()")

        indexDef.entityFieldDefs.forEach { fieldDef ->

            val collectionFieldName = fieldDef.dbColumnFieldDef.tableColumnName
            val fieldWriterClassField = fieldDef.fieldWriterClassField

            if (fieldWriterClassField != null) {

                val fieldWriterClassFieldName = fieldWriterClassField.classFieldName
                appendLine("        query.append(\"$collectionFieldName\", this.$fieldWriterClassFieldName.writeField(${fieldDef.classFieldDef.classFieldName}))")
            } else {

                appendLine("        query.append(\"$collectionFieldName\", ${renderWriteConversionForImplicitField(fieldDef)})")

            }

        }

        appendLine("        return find(query)")
        blankLine()
        appendLine("    }")

    }


    private fun renderDeleteByForIndex(indexDef: IndexDef) {

        if (this.entityDef.isNotDeletable) {
            return
        }

        if (indexDef.isForIdAndVersion) {
            return
        }

        addImportFor(Fqcns.MONGO_DELETE_RESULT)

        val functionParameters = buildFunctionParametersFrom(indexDef.classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(indexDef.classFieldDefs)

        blankLine()
        blankLine()
        appendLine("    fun deleteBy$fieldNamesAnded($functionParameters): DeleteResult {")
        blankLine()
        appendLine("        val query = Document()")

        indexDef.entityFieldDefs.forEach { fieldDef ->

            val collectionFieldName = fieldDef.dbColumnFieldDef.tableColumnName
            val fieldWriterClassField = fieldDef.fieldWriterClassField

            if (fieldWriterClassField != null) {

                val fieldWriterClassFieldName = fieldWriterClassField.classFieldName
                appendLine("        query.append(\"$collectionFieldName\", this.$fieldWriterClassFieldName.writeField(${fieldDef.classFieldDef.classFieldName}))")

            } else {

                appendLine("        query.append(\"$collectionFieldName\", ${renderWriteConversionForImplicitField(fieldDef)})")

            }

        }

        appendLine("        return deleteMany(query)")
        blankLine()
        appendLine("    }")

    }


    private fun renderCountWithFilter() {

        addImportFor(Fqcns.BSON)

        blankLine()
        blankLine()
        appendLine("    fun count(filter: ${this.entityDef.entityFilterClassDef.uqcn}): Long {")
        blankLine()
        appendLine("        val bsonFilter = filter.asBson(this.fieldConverter)")
        appendLine("        return super.count(bsonFilter)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_findAllBy() {

        addImportFor(Fqcns.BSON)

        blankLine()
        blankLine()
        appendLine("    fun findAllBy(filter: ${this.entityDef.entityFilterClassDef.uqcn}): List<${this.entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        val bsonFilter = filter.asBson(this.fieldConverter)")
        appendLine("        return super.find(bsonFilter)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_findAllByWithPageable() {

        addImportFor(Fqcns.BSON)
        addImportFor(Fqcns.SPRING_PAGE)
        addImportFor(Fqcns.SPRING_PAGEABLE)

        blankLine()
        blankLine()
        appendLine("    fun findAllBy(filter: ${this.entityDef.entityFilterClassDef.uqcn}, pageable: Pageable): Page<${this.entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        val bsonFilter = filter.asBson(this.fieldConverter)")
        appendLine("        return super.findPage(bsonFilter, pageable)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_findAllWithPageable() {

        addImportFor(Fqcns.SPRING_PAGE)
        addImportFor(Fqcns.SPRING_PAGEABLE)

        blankLine()
        blankLine()
        appendLine("    fun findAll(pageable: Pageable): Page<${this.entityDef.entityUqcn}> {")
        blankLine()
        appendLine("        return super.findPage(pageable)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_setFields() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        addImportFor(Fqcns.BSON)

        blankLine()
        blankLine()

        if (this.entityDef.withVersionHistory.value) {

            appendLine("    fun setFields(updater: ${this.entityDef.entityUpdaterClassDef.uqcn}, createHistoryRecord: Boolean = true) {")
            blankLine()
            appendLine("        val bson = updater.asBson(this.fieldConverter, incrementVersion = createHistoryRecord)")

            if (this.entityDef.versioned.value) {

                blankLine()
                appendLine("        if (updater.version != null) {")
                appendLine("            super.updateOneByIdAndVersion(updater.id, updater.version!!, bson, createHistoryRecord)")
                appendLine("        } else {")
                appendLine("            super.updateOneById(updater.id, bson, createHistoryRecord)")
                appendLine("        }")

            } else {

                appendLine("        super.updateOneById(updater.id, bson, createHistoryRecord)")

            }

        } else {

            appendLine("    fun setFields(updater: ${this.entityDef.entityUpdaterClassDef.uqcn}) {")
            blankLine()
            appendLine("        val bson = updater.asBson(this.fieldConverter)")

            if (this.entityDef.versioned.value) {

                blankLine()
                appendLine("        if (updater.version != null) {")
                appendLine("            super.updateOneByIdAndVersion(updater.id, updater.version!!, bson)")
                appendLine("        } else {")
                appendLine("            super.updateOneById(updater.id, bson)")
                appendLine("        }")

            } else {

                appendLine("        super.updateOneById(updater.id, bson)")

            }

        }

        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_setFieldsInBulk() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        addImportFor(Fqcns.MONGO_BULK_WRITE_RESULT)
        addImportFor(Fqcns.MONGO_UPDATE_ONE_MODEL)
        addImportFor(Fqcns.MONGO_UPDATE_OPTIONS)

        blankLine()
        blankLine()
        appendLine("    /**")
        appendLine("     * CAUTION: This doesn't work for entities with history. To be implemented when needed.")
        appendLine("     */")
        appendLine("    fun setFieldsInBulk(updaters: List<${this.entityDef.entityUpdaterClassDef.uqcn}>): BulkWriteResult {")
        blankLine()
        appendLine("        val updateOneModels = updaters.map { updater ->")
        blankLine()
        appendLine("            val filter = updater.filterBson()")
        appendLine("            val update = updater.asBson(this.fieldConverter, incrementVersion = false)")
        blankLine()
        appendLine("            val options = UpdateOptions().upsert(true)")
        blankLine()
        appendLine("            UpdateOneModel<Document>(filter, update, options)")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("        return bulkWrite(updateOneModels)")
        blankLine()
        appendLine("    }")

    }


    private fun renderUpsertsForIndexes() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        this.entityDef.databaseIndexDefs.filter{ it.isUnique }.forEach { this.renderUpsertForUniqueFields(it.indexDef.entityFieldDefs) }

    }


    private fun renderUpsertForUniqueFields(entityFieldDefs: List<EntityFieldDef>) {

        addImportFor(Fqcns.MONGO_FIND_ONE_AND_UPDATE_OPTIONS)
        addImportFor(Fqcns.MONGO_RETURN_DOCUMENT)

        val fieldNamesAnded = fieldNamesAnded(entityFieldDefs.map { it.classFieldDef })

        blankLine()
        blankLine()
        appendLine("    fun upsertBy$fieldNamesAnded(upsertEntity: ${this.entityDef.entityUqcn}): ${this.entityDef.entityUqcn} {")
        blankLine()
        appendLine("        val filter = Document()")

        entityFieldDefs.forEach { entityFieldDef ->

            if (entityFieldDef.classFieldDef.nullable) {

                appendLine("        upsertEntity.${entityFieldDef.classFieldDef.classFieldName}?.let { fieldValue -> filter.put(\"${entityFieldDef.dbColumnFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, "fieldValue")}) }")

            } else {

                appendLine("        filter.put(\"${entityFieldDef.dbColumnFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, "upsertEntity." + entityFieldDef.classFieldDef.classFieldName + "")})")

            }

        }

        blankLine()
        appendLine("        val update = toUpsertDocumentFrom(upsertEntity)")
        blankLine()
        appendLine("        val options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(true)")
        blankLine()
        appendLine("        return findOneAndUpdate(filter, update, options)")
        blankLine()
        appendLine("    }")

        addImportFor(Fqcns.MONGO_BULK_WRITE_RESULT)
        addImportFor(Fqcns.MONGO_UPDATE_ONE_MODEL)
        addImportFor(Fqcns.MONGO_UPDATE_OPTIONS)

        blankLine()
        blankLine()
        appendLine("    fun upsertManyBy$fieldNamesAnded(entities: List<${this.entityDef.entityUqcn}>): BulkWriteResult {")
        blankLine()
        appendLine("        val updateOneModels = entities.map { entity ->")
        blankLine()
        appendLine("            val filter = Document()")

        entityFieldDefs.forEach { entityFieldDef ->

            if (entityFieldDef.classFieldDef.nullable) {

                appendLine("            entity.${entityFieldDef.classFieldDef.classFieldName}?.let { fieldValue -> filter.put(\"${entityFieldDef.dbColumnFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, "fieldValue")}) }")

            } else {

                appendLine("            filter.put(\"${entityFieldDef.dbColumnFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, "entity." + entityFieldDef.classFieldDef.classFieldName + "")})")

            }

        }

        blankLine()
        appendLine("            val update = toUpsertDocumentFrom(entity)")
        blankLine()
        appendLine("            val options = UpdateOptions().upsert(true)")
        blankLine()
        appendLine("            UpdateOneModel<Document>(filter, update, options)")
        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("        return bulkWrite(updateOneModels)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_findOneByForFields(entityFieldDefs: List<EntityFieldDef>) {

        addImportFor(Sort::class.java)

        val classFieldDefs: List<ClassFieldDef> = entityFieldDefs.map { it.classFieldDef }
        val methodParameters = buildFunctionParametersFrom(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)

        blankLine()
        blankLine()
        appendLine("    fun findOneOrNullBy$fieldNamesAnded($methodParameters, orderBy: Sort? = null): ${this.entityDef.entityUqcn}? {")
        blankLine()
        appendLine("        val query = Document()")

        entityFieldDefs.forEach { fieldDef ->

            val fieldWriterClassField = fieldDef.fieldWriterClassField

            if (fieldWriterClassField != null) {

                val fieldWriterClassFieldName = fieldWriterClassField.classFieldName
                appendLine("        query.append(\"${fieldDef.dbColumnFieldDef.tableColumnName}\", this.$fieldWriterClassFieldName.writeField(${fieldDef.classFieldDef.classFieldName}))")
            } else {

                appendLine("        query.append(\"${fieldDef.dbColumnFieldDef.tableColumnName}\", ${renderWriteConversionForImplicitField(fieldDef)})")

            }

        }

        appendLine("        return findOneOrNull(query, orderBy = orderBy)")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun findOneBy$fieldNamesAnded($methodParameters, orderBy: Sort? = null): ${this.entityDef.entityUqcn} {")

        blankLine()
        appendLine("        val query = Document()")

        entityFieldDefs.forEach { fieldDef ->

            val fieldWriterClassField = fieldDef.fieldWriterClassField

            if (fieldWriterClassField != null) {

                val fieldWriterClassFieldName = fieldWriterClassField.classFieldName
                appendLine("        query.append(\"${fieldDef.dbColumnFieldDef.tableColumnName}\", this.$fieldWriterClassFieldName.writeField(${fieldDef.classFieldDef.classFieldName}))")
            } else {

                appendLine("        query.append(\"${fieldDef.dbColumnFieldDef.tableColumnName}\", ${renderWriteConversionForImplicitField(fieldDef)})")

            }

        }

        appendLine("        return findOne(query, orderBy = orderBy)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFieldWrite(
            entityFieldDef: EntityFieldDef,
            fieldName: String = entityFieldDef.classFieldDef.classFieldName.value,
            applyTextCase: Boolean = false
    ): String {

        val fieldWriterClassField = entityFieldDef.fieldWriterClassField

        return if (fieldWriterClassField != null) {

            "this.${fieldWriterClassField.classFieldName}.writeField($fieldName)"

        } else {

            renderWriteConversionForImplicitField(entityFieldDef, fieldName, applyTextCase = applyTextCase)

        }

    }


    private fun renderExistsByForFields(fieldDefSupplier: () -> List<EntityFieldDef>) {

        val classFieldDefs = fieldDefSupplier().map { it.classFieldDef }
        val methodParameters = buildMethodParametersWithUnwrappedOptionalsFrom(classFieldDefs)
        val fieldNamesAnded = fieldNamesAnded(classFieldDefs)

        blankLine()
        blankLine()
        appendLine("    fun existsBy$fieldNamesAnded($methodParameters): Boolean {")
        blankLine()
        appendLine("        val query = Document()")

        fieldDefSupplier().forEach { fieldDef ->
            appendLine("        query.append(\"${fieldDef.dbColumnFieldDef.tableColumnName}\", ${renderFieldWrite(fieldDef)})")
        }

        appendLine("        return exists(query)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_toDocumentFrom() {

        addImportFor(Fqcns.BSON_DOCUMENT)

        blankLine()
        blankLine()
        appendLine("    override fun toDocumentFrom(entity: ${this.entityDef.entityUqcn}): Document {")
        blankLine()

        if (this.entityHierarchy.hasSubclasses()) {

            var startOfIfClause = "if"

            for (entity in this.entityHierarchy.concreteEntityDefs) {

                addImportFor(entity.entityClassDef.fqcn)

                appendLine("        $startOfIfClause (entity is ${entity.entityUqcn}) {")
                blankLine()
                appendLine("            return toDocumentFrom${entity.entityUqcn}(entity)")
                blankLine()

                startOfIfClause = "} else if"

            }

            appendLine("        } else {")
            blankLine()
            appendLine("            throw IllegalStateException(\"Unknown type in class hierarchy: \" + entity::class.java)")
            blankLine()
            appendLine("        }")

        } else {

            appendLine("        val document = Document()")
            blankLine()

            if (this.entityDef.typeDiscriminatorOrNull != null) {
                appendLine("        document.append(\"TYP\", \"${this.entityDef.typeDiscriminatorOrNull}\")")
            }

            this.entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.forEach { writeFieldToDocument(it, applyTextCase = true) }

            blankLine()
            appendLine("        return document")

        }

        blankLine()
        appendLine("    }")

        if (this.entityHierarchy.hasSubclasses()) {
            this.entityHierarchy.concreteEntityDefs.forEach { this.renderToDocumentForEntity(it) }
        }

    }


    private fun renderFunction_toUpsertDocumentFrom() {

        addImportFor(Fqcns.BSON_DOCUMENT)

        blankLine()
        blankLine()
        appendLine("    override fun toUpsertDocumentFrom(entity: ${this.entityDef.entityUqcn}): Document {")
        blankLine()

        if (this.entityHierarchy.hasSubclasses()) {

            var startOfIfClause = "if"

            for (entity in this.entityHierarchy.concreteEntityDefs) {

                addImportFor(entity.entityClassDef.fqcn)

                appendLine("        $startOfIfClause (entity is ${entity.entityUqcn}) {")
                blankLine()
                appendLine("            return toUpsertDocumentFrom${entity.entityUqcn}(entity)")
                blankLine()

                startOfIfClause = "} else if"

            }

            appendLine("        } else {")
            blankLine()
            appendLine("            throw IllegalStateException(\"Unknown type in class hierarchy: \" + entity::class.java)")
            blankLine()
            appendLine("        }")

        } else {

            appendLine("        val modifiableFieldsDocument = Document()")
            appendLine("        val unmodifiableFieldsDocument = Document()")
            blankLine()

            if (this.entityDef.typeDiscriminatorOrNull != null) {
                appendLine("        unmodifiableFieldsDocument.append(\"TYP\", \"${this.entityDef.typeDiscriminatorOrNull}\")")
            }

            this.entityDef.allModifiableFieldDef
                    .filterNot(byName(ClassFieldName.lastModifiedTimestampUtc))
                    .forEach { fieldDef -> writeFieldToDocument(fieldDef, "modifiableFieldsDocument", applyTextCase = true) }

            this.entityDef.allUnmodifiableFieldDef
                    .filterNot(byName(ClassFieldName.createdTimestampUtc))
                    .filterNot(byName(ClassFieldName.lastModifiedTimestampUtc))
                    .filterNot(byName(ClassFieldName.version))
                    .filterNot { it.isDerived.value }
                    .forEach { fieldDef -> writeFieldToDocument(fieldDef, "unmodifiableFieldsDocument", applyTextCase = true) }

            blankLine()

            appendLine("        unmodifiableFieldsDocument.append(\"${CollectionFieldName.createdTimestampUtc}\", Instant.now())")

            if (this.entityDef.hasModifiableFields()) {
                appendLine("        unmodifiableFieldsDocument.append(\"${CollectionFieldName.lastModifiedTimestampUtc}\", Instant.now())")
            }

            blankLine()
            appendLine("        return Document()")
            appendLine("                .append(\"\\\$setOnInsert\", unmodifiableFieldsDocument)")
            appendLine("                .append(\"\\\$set\", modifiableFieldsDocument)")

            if (this.entityDef.versioned.value) {
                appendLine("                .append(\"\\\$inc\", Document(\"v\", 1))")
            }

        }

        blankLine()
        appendLine("    }")

        if (this.entityHierarchy.hasSubclasses()) {
            this.entityHierarchy.concreteEntityDefs.forEach { this.renderToUpsertDocumentForEntity(it) }
        }

    }


    private fun renderToDocumentForEntity(entity: EntityDef) {

        blankLine()
        blankLine()
        appendLine("    private fun toDocumentFrom${entity.entityUqcn}(entity: ${entity.entityUqcn}): Document {")
        blankLine()
        appendLine("        val document = Document()")
        blankLine()
        appendLine("        document.append(\"TYP\", \"${entity.typeDiscriminator}\")")

        entity.allEntityFieldsSorted.filterNot { it.isDerived.value }.forEach { writeFieldToDocument(it) }

        blankLine()
        appendLine("        return document")
        blankLine()
        appendLine("    }")


    }


    private fun renderToUpsertDocumentForEntity(entity: EntityDef) {

        blankLine()
        blankLine()
        appendLine("    private fun toUpsertDocumentFrom${entity.entityUqcn}(entity: ${entity.entityUqcn}): Document {")
        blankLine()
        appendLine("        val modifiableFieldsDocument = Document()")
        appendLine("        val unmodifiableFieldsDocument = Document()")
        blankLine()
        appendLine("        unmodifiableFieldsDocument.append(\"TYP\", \"${entity.typeDiscriminator}\")")

        entity.allModifiableFieldDef
                .filterNot(byName(ClassFieldName.lastModifiedTimestampUtc))
                .forEach { fieldDef -> writeFieldToDocument(fieldDef, "modifiableFieldsDocument") }

        entity.allUnmodifiableFieldDef
                .filterNot(byName(ClassFieldName.createdTimestampUtc))
                .filterNot(byName(ClassFieldName.lastModifiedTimestampUtc))
                .filterNot(byName(ClassFieldName.version))
                .filterNot { it.isDerived.value }
                .forEach { fieldDef -> writeFieldToDocument(fieldDef, "unmodifiableFieldsDocument") }

        blankLine()

        appendLine("        unmodifiableFieldsDocument.append(\"${CollectionFieldName.createdTimestampUtc}\", Instant.now())")

        if (entity.hasModifiableFields()) {
            appendLine("        unmodifiableFieldsDocument.append(\"${CollectionFieldName.lastModifiedTimestampUtc}\", Instant.now())")
        }

        blankLine()
        appendLine("        return Document()")
        appendLine("                .append(\"\\\$setOnInsert\", unmodifiableFieldsDocument)")
        appendLine("                .append(\"\\\$set\", modifiableFieldsDocument)")

        if (entity.versioned.value) {
            appendLine("                .append(\"\\\$inc\", Document(\"v\", 1))")
        }

        blankLine()
        appendLine("    }")


    }


    private fun byName(fieldName: ClassFieldName): (EntityFieldDef) -> Boolean {

        return { fieldDef -> fieldDef.classFieldName == fieldName }

    }


    private fun writeFieldToDocument(
            entityFieldDef: EntityFieldDef,
            nameOfDocument: String = "document",
            applyTextCase: Boolean = false
    ) {

        if (entityFieldDef.classFieldDef.nullable) {

            appendLine("        entity.${entityFieldDef.classFieldName}?.let { ${entityFieldDef.classFieldName} -> $nameOfDocument.append(\"${entityFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, applyTextCase = applyTextCase)}) }")

        } else {

            if (entityFieldDef.classFieldDef.isList) {

                appendLine("        $nameOfDocument.append(\"${entityFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, "entity.${entityFieldDef.classFieldName}", applyTextCase = applyTextCase)})")

            } else {

                appendLine("        $nameOfDocument.append(\"${entityFieldDef.tableColumnName}\", ${renderFieldWrite(entityFieldDef, "entity.${entityFieldDef.classFieldName}", applyTextCase = applyTextCase)})")

            }

        }

    }


    private fun renderFunction_toEntityFrom() {

        addImportFor(Fqcns.BSON_DOCUMENT)

        blankLine()
        blankLine()
        appendLine("    override fun toEntityFrom(document: Document): ${this.entityDef.entityUqcn} {")
        blankLine()

        if (this.entityHierarchy.hasSubclasses()) {

            appendLine("        val typeDiscriminator = readString(\"TYP\", \"typeDiscriminator\", document)")
            blankLine()
            appendLine("        when (typeDiscriminator) {")

            this.entityHierarchy.concreteEntityDefs.forEach { entity ->

                addImportFor(entity.entityClassDef.fqcn)
                val typeDiscriminator = entity.typeDiscriminatorOrNull ?: throw RuntimeException("Expected entity to have a type discriminator: " + entity.entityBaseName)

                blankLine()
                appendLine("            \"$typeDiscriminator\" -> ")
                appendLine("                return ${entity.entityUqcn.firstToLower()}From(document)")

            }

            appendLine("            else -> throw RuntimeException(\"A record exists with id \" + document.get(\"_id\") + \" but it has an unknown type discriminator: \" + typeDiscriminator)")
            appendLine("        }")
            blankLine()
            appendLine("    }")

            this.entityHierarchy.concreteEntityDefs.forEach { this.renderFunction_entityFrom(it) }

        } else {

            this.entityDef.allEntityFieldsSorted.forEach { renderReadField(it.classFieldDef, it.dbColumnFieldDef, this) }

            renderCallToEntityConstructor(this.entityDef, 8, this)
            blankLine()
            appendLine("    }")

        }

    }


    private fun renderFunction_entityFrom(entity: EntityDef) {

        addImportFor(entity.entityClassDef.fqcn)

        blankLine()
        blankLine()
        appendLine("    private fun ${entity.entityUqcn.firstToLower()}From(document: Document): ${entity.entityUqcn} {")
        blankLine()

        entity.allEntityFieldsSorted.forEach { renderReadField(it.classFieldDef, it.dbColumnFieldDef, this) }

        EntityRendererHelper.renderCallToEntityConstructor(entity, 8, this)
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_convertClassFieldNameToCollectionFieldName() {

        blankLine()
        blankLine()
        appendLine("    override fun convertClassFieldNameToCollectionFieldName(classFieldName: String): String {")
        blankLine()
        appendLine("        return ${this.entityDef.metaClassDef.uqcn}.convertClassFieldNameToCollectionFieldName(classFieldName)")
        blankLine()
        appendLine("    }")

    }


    private fun renderFunction_getTypeDiscriminator() {

        blankLine()
        blankLine()

        if (this.entityDef.typeDiscriminatorOrNull != null) {
            appendLine("    override val typeDiscriminator = \"${this.entityDef.typeDiscriminator}\"")
        } else {
            appendLine("    override val typeDiscriminator = null")
        }

    }


    private fun renderFunction_deleteAll() {

        if (this.entityDef.allowDeleteAll.value) {
            blankLine()
            blankLine()
            appendLine("    fun deleteAll() {")
            blankLine()
            appendLine("        deleteMany(Document())")
            blankLine()
            appendLine("    }")

        }

    }


    private fun renderExistsByForeignKeyFields() {

        this.entityDef.allForeignKeyEntityFieldDefs.forEach { entityFieldDef ->

            val fieldName = entityFieldDef.classFieldName

            blankLine()
            blankLine()
            appendLine("    fun existsBy${fieldName.firstToUpper()}(${fieldName}: DomainId): Boolean {")
            blankLine()
            appendLine("        val filter = Document(\"${entityFieldDef.tableColumnName}\", $fieldName)")
            appendLine("        return exists(filter)")
            blankLine()
            appendLine("    }")

        }

    }


    private fun renderWriteConversionForImplicitField(
        entityFieldDef: EntityFieldDef,
        fieldName: String = entityFieldDef.classFieldDef.classFieldName.value,
        requiresCast: Boolean = false,
        applyTextCase: Boolean = false
    ): String {

        val fieldType = entityFieldDef.classFieldDef.fieldType
        val castPrefix = if (requiresCast) "(${fieldType.unqualifiedToString}) $fieldName" else fieldName
        val fullCastPrefix = if (requiresCast) "($fieldName as ${fieldType.unqualifiedToString})" else fieldName

        when {
            fieldType is ListFieldType -> {

                val listElementNonPrimitiveType = fieldType.parameterFieldType

                if (listElementNonPrimitiveType is EnumFieldType) {

                    return "$fullCastPrefix.map { it.name }"

                } else {

//                    if (listElementNonPrimitiveType is SimpleWrapperFieldType) {
//                        return "$fullCastPrefix.map { it.value }"
//                    }

                    if (listElementNonPrimitiveType is InstantFieldType) {
                        return "$fullCastPrefix.map { Date.from(it) }"
                    }

                    if (listElementNonPrimitiveType is LocalDateFieldType) {

                        addImportFor(ZoneId::class.java)
                        return "$fullCastPrefix.map { ld -> Date.from(ld.atStartOfDay(ZoneId.of(\"UTC\")).toInstant()) }"

                    }

                    if (listElementNonPrimitiveType is PeriodFieldType) {

                        addImportFor(Period::class.java)
                        return String.format("$fullCastPrefix.map { it.toString() }")

                    }

                }

                return fieldName


            }
            fieldType is MapFieldType -> {

                val mapKeyNonPrimitiveType = fieldType.keyFieldType
                val mapValueNonPrimitiveType = fieldType.valueFieldType

                val sb = StringBuilder(fullCastPrefix)

                if (mapKeyNonPrimitiveType is EnumFieldType) {

                    sb.append(".mapKeys { it.key.name }")

                } else {

                    when {
                        mapKeyNonPrimitiveType is StringFieldType -> {
                            sb.append(".mapKeys { it.key.value }")
                        }
                        mapKeyNonPrimitiveType is InstantFieldType -> {
                            sb.append(".mapKeys { Date.from(it.key.value) }")
                        }
                        mapKeyNonPrimitiveType is LocalDateFieldType -> {
                            sb.append(".mapKeys { Date.from(it.key.value.atStartOfDay(ZoneId.of(\"UTC\")).toInstant()) }")
                        }
                        mapKeyNonPrimitiveType is PeriodFieldType -> {
                            sb.append(".mapKeys { it.key.value.toString() }")
                        }
                    }

                }

                if (mapValueNonPrimitiveType is EnumFieldType) {

                    sb.append(".mapValues { it.value.name }")

                } else {

                    when {
                        mapValueNonPrimitiveType is StringFieldType -> {
                            sb.append(".mapValues { it.value.value }")
                        }
                        mapValueNonPrimitiveType is InstantFieldType -> {
                            sb.append(".mapValues { Date.from(it.value.value) }")
                        }
                        mapValueNonPrimitiveType is LocalDateFieldType -> {
                            sb.append(".mapValues { Date.from(it.value.value.atStartOfDay(ZoneId.of(\"UTC\")).toInstant()) }")
                        }
                        mapValueNonPrimitiveType is PeriodFieldType -> {
                            sb.append(".mapValues { it.value.value.toString() }")
                        }
                    }

                }

                return sb.toString()

            }
            else -> {

                when {
                    fieldType is StringFieldType -> {
                        return "$fullCastPrefix.value"
                    }
                    fieldType is DomainIdFieldType -> {
                        return "$fullCastPrefix.toObjectId()"
                    }
                    fieldType is EnumFieldType -> {
                        return "$fullCastPrefix.name"
                    }
                    fieldType is InstantFieldType -> {

                        addImportFor(Date::class.java)
                        return "Date.from($castPrefix)"

                    }
                    fieldType is LocalDateFieldType -> {

                        addImportFor(ZoneId::class.java)
                        return "Date.from($fullCastPrefix.atStartOfDay(ZoneId.of(\"UTC\")).toInstant())"

                    }
                    fieldType is PeriodFieldType -> {
                        return "$fieldName.toString()"

                    }
                    fieldType is StringFieldType -> {

                        return if (applyTextCase) {

                            val suffix = when (entityFieldDef.classFieldDef.textCase) {
                                TextCase.LOWER -> ".toLowerCase()"
                                TextCase.UPPER -> ".toUpperCase()"
                                else -> ""
                            }

                            "$fieldName$suffix"

                        } else {

                            fieldName

                        }

                    }
                    else -> return fieldName

                }

            }

        }

    }


}
