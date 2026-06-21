package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.DatabaseIndexDef
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.InlineEditDtoDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.JoinFetchDtoFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import java.time.Instant

class CrudServiceRenderer(
    private val entityDef: EntityDef,
    private val applicationModelDef: ApplicationModelDef
) : AbstractKotlinRenderer(
    entityDef.crudServiceClassDef
) {


    private val primaryKeyFieldNamesAndTypesCsv = if (entityDef.hasCompositePrimaryKey) {
        "primaryKey: ${entityDef.entityPkClassDef.uqcn}"
    } else {
        fieldNamesAndTypesCsv(entityDef.primaryKeyClassFields)
    }


    private val primaryKeyFieldNamesCsv = if (entityDef.hasCompositePrimaryKey) {
        "primaryKey"
    } else {
        fieldNamesCsv(entityDef.primaryKeyClassFields)
    }


    private val primaryKeyEditDtoFieldNamesCsv = if (entityDef.hasCompositePrimaryKey) {
        val args = entityDef.primaryKeyClassFields.joinToString(", ") { "editDto.${it.classFieldName.value}" }
        "${entityDef.entityPkClassDef.uqcn}($args)"
    } else {
        entityDef.primaryKeyClassFields.joinToString(", ") { "editDto.${it.classFieldName.value}" }
    }


    init {

        addConstructorArg(ClassFieldDef.aClassField("entityRepo", this.entityDef.entityRepoFqcn).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("maiaProblems", Fqcns.MAIA_PROBLEMS).privat().build())

        this.applicationModelDef.entitiesThatReference(this.entityDef).forEach { referencingEntityDef ->
            addConstructorArg(ClassFieldDef.aClassField(referencingEntityDef.entityRepoFqcn.uqcn.firstToLower(), referencingEntityDef.entityRepoFqcn).privat().build())
        }

        if (this.entityDef.crudDef.withCrudListener.value) {
            addConstructorArg(ClassFieldDef.aClassField(this.entityDef.crudNotifierClassDef.uqcn.firstToLower(), this.entityDef.crudNotifierClassDef.fqcn).privat().build())
        }

        this.entityDef.primaryKeyClassFields.forEach { addImportFor(it.fieldType) }

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.SLF4J_LOGGER_FACTORY)

        blankLine()
        blankLine()
        appendLine("    private val logger = LoggerFactory.getLogger(${this.classDef.uqcn}::class.java)")

    }


    override fun renderFunctions() {

        `render create by DTO`()
        `render the create function`()
        `render existsBy for unique indexes`()
        `render the fetchForEdit function`()
        `render the update function`()
        `render private join reconciliation functions`()
        `render the inline update functions`()
        `render the setFields function`()
        `render the delete function`()

    }


    private fun `render create by DTO`() {

        val createApiDef = this.entityDef.entityCrudApiDef?.createApiDef ?: return

        blankLine()
        blankLine()
        appendPreAuthorize(createApiDef.crudApiDef.authorityDef)
        appendLine("    fun create(createDto: ${createApiDef.requestDtoDef.uqcn}): ${this.entityDef.entityUqcn} {")

        if (this.entityDef.hasCreatedByIdField || this.entityDef.hasCreatedByUsernameField) {

            addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)

            blankLine()
            appendLine("        val currentUser = CurrentUserHolder.currentUser")
            blankLine()
            appendLine($$"        logger.info(\"BEGIN: create $${this.entityDef.entityBaseName}. createdBy=${currentUser.username}, dto=$createDto\")")

        } else {

            blankLine()
            appendLine($$"        logger.info(\"BEGIN: create $${this.entityDef.entityBaseName}. dto=$createDto\")")

        }

        val currentUserOrBlank = if (this.entityDef.hasCreatedByIdField || this.entityDef.hasLastModifiedByIdField) {
            ", currentUser"
        } else {
            ""
        }

        blankLine()
        appendLine("        val entity: ${this.entityDef.entityUqcn} = buildEntity(createDto${currentUserOrBlank})")
        blankLine()

        if (createApiDef.entityDef.manyToManyAssociations.isNotEmpty()) {

            appendLine("        create(entity)")

            createApiDef.entityDef.manyToManyAssociations.forEach { manyToManyEntityDef ->

                val otherSide = manyToManyEntityDef.otherSideFrom(this.entityDef)
                val thisSideEntityIdFieldName = manyToManyEntityDef.idFieldName(this.entityDef)
                val otherSideFieldName = otherSide.fieldName
                val otherSideDtoFieldName = "${otherSideFieldName}EntityIds"
                val joinEntityClass = manyToManyEntityDef.entityDef.entityUqcn
                addImportFor(manyToManyEntityDef.entityDef.entityFqcn)
                val joinRepoFieldName = manyToManyEntityDef.entityDef.entityRepoFqcn.uqcn.firstToLower()

                blankLine()

                if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
                    val isSystemManaged = manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
                    if (isSystemManaged) addImportFor<Instant>()
                    val joinDtoFieldName = "${otherSideFieldName}Entities"
                    val effectiveFromValue = if (isSystemManaged) "Instant.now()" else "joinDto.effectiveFrom"
                    val effectiveToValue = if (isSystemManaged) "null" else "joinDto.effectiveTo"
                    val extraArgs = manyToManyEntityDef.entityDef.allFieldsRequiredInCreateRequest
                        .filterNot { it.classFieldDef.classFieldName.value == thisSideEntityIdFieldName }
                        .filterNot { it.classFieldDef.classFieldName.value == otherSideFieldName }
                        .filterNot { it.classFieldDef.classFieldName.value == "effectiveFrom" }
                        .filterNot { it.classFieldDef.classFieldName.value == "effectiveTo" }
                        .onEach { addImportFor(it.classFieldDef.fieldType) }
                        .map { it.classFieldDef.classFieldName.value to "joinDto.${it.classFieldDef.classFieldName.value}" }
                    appendLine("        createDto.${joinDtoFieldName}.forEach { joinDto ->")
                    appendLine("            this.${joinRepoFieldName}.insert(")
                    appendLine("                ${joinEntityClass}.newInstance(")
                    renderNewInstanceArgsMultiLine(
                        indentSize = 20,
                        listOf(
                            "effectiveFrom" to effectiveFromValue,
                            "effectiveTo" to effectiveToValue,
                            thisSideEntityIdFieldName to "entity.id",
                            otherSideFieldName to "joinDto.${otherSideFieldName}EntityId"
                        ) + extraArgs
                    )
                    appendLine("                )")
                    appendLine("            )")
                    appendLine("        }")
                } else {
                    appendLine("        createDto.${otherSideDtoFieldName}.forEach { $otherSideFieldName ->")
                    appendLine("            this.${joinRepoFieldName}.insert(")
                    appendLine("                ${joinEntityClass}.newInstance(")
                    renderNewInstanceArgsMultiLine(
                        indentSize = 20,
                        thisSideEntityIdFieldName to "entity.id",
                        otherSideFieldName to otherSideFieldName
                    )
                    appendLine("                )")
                    appendLine("            )")
                    appendLine("        }")
                }

            }

            blankLine()
            appendLine("        return entity")

        } else {

            appendLine("        return create(entity)")

        }

        blankLine()
        appendLine("    }")

        `render function buildEntity`(createApiDef)

    }



    private fun `render the create function`() {

        blankLine()
        blankLine()
        appendLine("    fun create(entity: ${this.entityDef.entityUqcn}): ${this.entityDef.entityUqcn} {")
        blankLine()
        appendLine("        this.entityRepo.insert(entity)")

        if (this.entityDef.crudDef.withCrudListener.value) {

            appendLine("        this.${this.entityDef.crudNotifierClassDef.uqcn.firstToLower()}.onEntityCreated(entity)")

        }

        appendLine("        return entity")
        blankLine()
        appendLine("    }")

    }


    private fun `render function buildEntity`(apiDef: EntityCreateApiDef) {

        addImportFor<Instant>()

        if (this.entityDef.hasCreatedByIdField || this.entityDef.hasLastModifiedByIdField) {

            addImportFor(Fqcns.MAIA_USER_DETAILS)

            append("""
                |
                |
                |    private fun buildEntity(
                |        createDto: ${apiDef.requestDtoDef.uqcn},
                |        currentUser: MaiaUserDetails
                |    ): ${this.entityDef.entityUqcn} {
                |
                |""".trimMargin()
            )

        } else {

            append("""
                |
                |
                |    private fun buildEntity(createDto: ${apiDef.requestDtoDef.uqcn}): ${this.entityDef.entityUqcn} {
                |
                |""".trimMargin()
            )

        }

        this.entityDef.allFieldsRequiredInCreateRequest.forEach { field ->

            addImportFor(field.classFieldDef.fieldType)

            if (field.isDerived.value) {

                val derivedFieldPlaceholder = derivedPlaceholderFor(field)
                appendLine("        val ${field.classFieldDef.classFieldName}: ${field.classFieldDef.unqualifiedToString} = $derivedFieldPlaceholder")

            } else {
                appendLine("        val ${field.classFieldDef.classFieldName}: ${field.classFieldDef.unqualifiedToString} = createDto.${field.classFieldDef.classFieldName}")
            }

        }

        if (this.entityDef.hasCreatedByIdField) {

            addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)

            appendLine("        val createdBy = currentUser.userId")

        }

        if (this.entityDef.hasCreatedByUsernameField) {

            addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)

            appendLine("        val createdByUsername = currentUser.username")

        }

        if (entityDef.hasSurrogatePrimaryKey) {
            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            appendLine("        val id = DomainId.newId()")
        }

        appendLine("        val createdTimestampUtc = Instant.now()")

        if (this.entityDef.hasLastModifiedByIdField) {
            appendLine("        val lastModifiedBy = currentUser.userId")
        }

        if (this.entityDef.hasLastModifiedByUsernameField) {
            appendLine("        val lastModifiedByUsername = currentUser.username")
        }

        if (this.entityDef.hasLastModifiedTimestampUtcField) {
            appendLine("        val lastModifiedTimestampUtc = createdTimestampUtc")
        }

        if (this.entityDef.hasLifecycleStateField) {
            addImportFor(Fqcns.MAIA_LIFECYCLE_STATE)
            appendLine("        val lifecycleState = LifecycleState.ACTIVE")
        }

        if (this.entityDef.hasVersionField) {
            appendLine("        val version = 1L")
        }

        val args = this.entityDef.allClassFieldsSorted.map { it.classFieldName.value }

        blankLine()
        appendLine("        return ${this.entityDef.entityUqcn}(")
        renderStrings(args, indent = 12)
        newLine()
        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun derivedPlaceholderFor(entityFieldDef: EntityFieldDef): String {

        return when (entityFieldDef.fieldType) {
            is BooleanFieldType -> TODO("YAGNI?")
            is BooleanTypeFieldType -> TODO("YAGNI?")
            is BooleanValueClassFieldType -> TODO("YAGNI?")
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> TODO("YAGNI?")
            is DoubleFieldType -> TODO("YAGNI?")
            is EnumFieldType -> TODO("YAGNI?")
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> TODO("YAGNI?")
            is FqcnFieldType -> TODO("YAGNI?")
            is JoinFetchDtoFieldType -> TODO("YAGNI?")
            is PkAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> TODO("YAGNI?")
            is IntFieldType -> TODO("YAGNI?")
            is IntTypeFieldType -> TODO("YAGNI?")
            is IntValueClassFieldType -> TODO("YAGNI?")
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> TODO("YAGNI?")
            is LongFieldType -> TODO("YAGNI?")
            is LongTypeFieldType -> TODO("YAGNI?")
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> TODO("YAGNI?")
            is PeriodFieldType -> TODO("YAGNI?")
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> "\"DERIVED\""
            is StringTypeFieldType -> "\"DERIVED\""
            is StringValueClassFieldType -> TODO("YAGNI?")
            is UrlFieldType -> TODO("YAGNI?")
        }

    }


    private fun `render existsBy for unique indexes`() {

        this.entityDef.uniqueIndexDefs.filter { it.withExistsEndpoint }.forEach { renderExistsByForUniqueIndex(it) }

    }


    private fun renderExistsByForUniqueIndex(databaseIndexDef: DatabaseIndexDef) {

        databaseIndexDef.indexDef.classFieldDefs.forEach { fieldDef -> addImportFor(fieldDef.fieldType)}

        val functionName = databaseIndexDef.existsByFunctionName
        val fieldNameAndTypesCsv = databaseIndexDef.indexDef.classFieldDefs.joinToString(", ") { "${it.classFieldName}: ${it.fieldType.unqualifiedToString}" }
        val fieldNamesCsv = databaseIndexDef.indexDef.classFieldDefs.map { it.classFieldName }.joinToString(", ")

        append("""
            |
            |
            |    fun $functionName($fieldNameAndTypesCsv): Boolean {
            |
            |        return this.entityRepo.$functionName(${fieldNamesCsv})
            |
            |    }
            |""".trimMargin())

    }


    private fun `render the fetchForEdit function`() {

        val fetchForEditDtoDef = this.entityDef.fetchForEditDtoDef
            ?: return

        addImportFor(fetchForEditDtoDef.dtoDef.fqcn)

        append("""
            |
            |
            |    fun fetchForEdit($primaryKeyFieldNamesAndTypesCsv): ${entityDef.fetchForEditDtoFqcn.uqcn} {
            |
            |        return this.entityRepo.fetchForEdit($primaryKeyFieldNamesCsv)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render the update function`() {

        val apiDef = this.entityDef.entityCrudApiDef?.updateApiDef
            ?: return

        addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)
        apiDef.requestDtoDef.let { dtoDef ->

            blankLine()
            blankLine()
            appendPreAuthorize(apiDef.crudApiDef.authorityDef)
            appendLine("    fun update(editDto: ${dtoDef.uqcn}) {")
            blankLine()

            this.entityDef.primaryKeyClassFields.forEach { field ->
                appendLine("        val ${field.classFieldName.value} = editDto.${field.classFieldName.value}")
            }

            if (this.entityDef.hasCompositePrimaryKey) {
                val pkConstructorArgs = entityDef.primaryKeyClassFields.joinToString(", ") { it.classFieldName.value }
                appendLine("        val primaryKey = ${entityDef.entityPkClassDef.uqcn}($pkConstructorArgs)")
            }

            if (this.entityDef.versioned.value) {

                appendLine("        val version = editDto.version")
                appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forPrimaryKey($primaryKeyFieldNamesCsv, version) {")

            } else {
                appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forPrimaryKey($primaryKeyFieldNamesCsv) {")
            }

            val manyToManyFieldNames = apiDef.entityDef.manyToManyAssociations.map { m2m ->
                val otherSide = m2m.otherSideFrom(this.entityDef)
                if (m2m.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) "${otherSide.fieldName}Entities"
                else "${otherSide.fieldName}EntityIds"
            }.toSortedSet()

            dtoDef.classFieldDefs
                .filterNot { entityDef.isPrimaryKey(it.classFieldName) }
                .filterNot { it.isVersionField }
                .filterNot { it.classFieldName.value in manyToManyFieldNames }
                .forEach { field -> appendLine("            ${field.classFieldName}(editDto.${field.classFieldName})") }

            if (this.entityDef.hasLastModifiedByIdField) {
                addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)
                appendLine("            lastModifiedBy(CurrentUserHolder.userId)")
            }

            if (this.entityDef.hasLastModifiedTimestampUtcField) {
                addImportFor<Instant>()
                appendLine("            lastModifiedTimestampUtc(Instant.now())")
            }

            appendLine("        }")
            blankLine()
            appendLine("        setFields(updater)")

        }

        apiDef.entityDef.manyToManyAssociations.forEach { manyToManyEntityDef ->

            val otherSide = manyToManyEntityDef.otherSideFrom(this.entityDef)
            val otherSideFieldName = otherSide.fieldName
            val joinEntityClass = manyToManyEntityDef.entityDef.entityUqcn
            val joinNamePrefix = joinEntityClass.value.removeSuffix("Entity")

            if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP && manyToManyEntityDef.entityDef.isDeletable) {
                val otherSideDtoFieldName = "${otherSideFieldName}Entities"
                append("""
                    |
                    |        reconcile${joinNamePrefix}Joins(id, editDto.${otherSideDtoFieldName})
                    |""".trimMargin())
            } else if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
                val otherSideDtoFieldName = "${otherSideFieldName}Entities"
                val thisSideFieldName = manyToManyEntityDef.idFieldName(this.entityDef)
                val thisSideFieldNameCapitalized = thisSideFieldName.replaceFirstChar { it.uppercaseChar() }
                appendLine("        //this.${joinEntityClass.value.replaceFirstChar { it.lowercaseChar() }}Repo.findBy${thisSideFieldNameCapitalized}(id).forEach { join ->")
                appendLine("        //    this.${joinEntityClass.value.replaceFirstChar { it.lowercaseChar() }}Repo.deleteByPrimaryKey(join.id)")
                appendLine("        //}")
                blankLine()
                appendLine("        //val new${joinNamePrefix}Joins = editDto.${otherSideDtoFieldName}.map { joinDto ->")
                appendLine("        //    ${joinEntityClass}.newInstance(${newInstanceArgsSingleLine("effectiveFrom" to "joinDto.effectiveFrom", "effectiveTo" to "joinDto.effectiveTo", thisSideFieldName to "id", otherSideFieldName to "joinDto.${otherSideFieldName}EntityId")})")
                appendLine("        //}")
                appendLine("        //this.${joinEntityClass.value.replaceFirstChar { it.lowercaseChar() }}Repo.bulkInsert(new${joinNamePrefix}Joins)")
            } else {
                val otherSideDtoFieldName = "${otherSideFieldName}EntityIds"
                append("""
                    |
                    |        reconcile${joinNamePrefix}Joins(id, editDto.${otherSideDtoFieldName})
                    |""".trimMargin())
            }

        }

        blankLine()
        appendLine("    }")

    }


    private fun `render private join reconciliation functions`() {

        val apiDef = this.entityDef.entityCrudApiDef?.updateApiDef
            ?: return

        apiDef.entityDef.manyToManyAssociations.forEach { manyToManyEntityDef ->

            val otherSide = manyToManyEntityDef.otherSideFrom(this.entityDef)
            val thisSideFieldName = manyToManyEntityDef.idFieldName(this.entityDef)
            val otherSideFieldName = otherSide.fieldName
            val thisSideFieldNameCapitalized = thisSideFieldName.replaceFirstChar { it.uppercaseChar() }
            val joinEntityClass = manyToManyEntityDef.entityDef.entityUqcn
            val joinRepoFieldName = manyToManyEntityDef.entityDef.entityRepoFqcn.uqcn.firstToLower()
            val joinNamePrefix = joinEntityClass.value.removeSuffix("Entity")

            if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP && manyToManyEntityDef.entityDef.isDeletable) {

                val isSystemManaged = manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
                val findByMethodName = if (isSystemManaged) "findEffectiveBy${thisSideFieldNameCapitalized}" else "findBy${thisSideFieldNameCapitalized}"
                val effectiveFromValue = if (isSystemManaged) "Instant.now()" else "joinDto.effectiveFrom"
                val effectiveToValue = if (isSystemManaged) "null" else "joinDto.effectiveTo"

                val joinDtoDef = apiDef.timestampedJoinRequestDtosByAssociation[manyToManyEntityDef]!!
                addImportFor(joinDtoDef.fqcn)
                addImportFor(Fqcns.MAIA_DOMAIN_ID)
                addImportFor(manyToManyEntityDef.entityDef.entityFqcn)
                if (isSystemManaged) addImportFor<Instant>()

                append("""
                    |
                    |
                    |    private fun reconcile${joinNamePrefix}Joins(
                    |        id: DomainId,
                    |        submitted: List<${joinDtoDef.uqcn}>
                    |    ) {
                    |
                    |        val existingById = this.${joinRepoFieldName}.${findByMethodName}(id).associateBy { it.id }
                    |        val submittedIds = submitted.mapNotNull { it.id }.toSet()
                    |""".trimMargin())

                if (isSystemManaged) {
                    append("""
                        |
                        |        existingById.keys.filterNot { it in submittedIds }.forEach {
                        |            this.${joinRepoFieldName}.closeEffectiveRange(it)
                        |        }
                        |""".trimMargin())
                } else {
                    append("""
                        |
                        |        existingById.keys.filterNot { it in submittedIds }.forEach {
                        |            this.${joinRepoFieldName}.deleteByPrimaryKey(it)
                        |        }
                        |""".trimMargin())
                }

                val extraReconcileArgs = manyToManyEntityDef.entityDef.allFieldsRequiredInCreateRequest
                    .filterNot { it.classFieldDef.classFieldName.value == thisSideFieldName }
                    .filterNot { it.classFieldDef.classFieldName.value == otherSideFieldName }
                    .filterNot { it.classFieldDef.classFieldName.value == "effectiveFrom" }
                    .filterNot { it.classFieldDef.classFieldName.value == "effectiveTo" }
                    .onEach { addImportFor(it.classFieldDef.fieldType) }
                    .map { it.classFieldDef.classFieldName.value to "joinDto.${it.classFieldDef.classFieldName.value}" }

                append("""
                    |
                    |        val newJoins = submitted.filter { it.id == null }.map { joinDto ->
                    |            ${joinEntityClass}.newInstance(
                    |""".trimMargin())

                renderNewInstanceArgsMultiLine(
                    indentSize = 16,
                    listOf(
                        "effectiveFrom" to effectiveFromValue,
                        "effectiveTo" to effectiveToValue,
                        thisSideFieldName to "id",
                        otherSideFieldName to "joinDto.${otherSideFieldName}EntityId"
                    ) + extraReconcileArgs
                )

                append("""
                    |            )
                    |        }
                    |
                    |        this.${joinRepoFieldName}.bulkInsert(newJoins)
                    |""".trimMargin())

                if (!isSystemManaged) {
                    append("""
                        |
                        |        submitted.filter { it.id != null }.forEach { joinDto ->
                        |            val joinId = joinDto.id!!
                        |            val existingJoin = existingById[joinId]
                        |                ?: throw this.maiaProblems.joinRecordNotFound("$joinEntityClass")
                        |
                        |            if (existingJoin.effectiveFrom != joinDto.effectiveFrom || existingJoin.effectiveTo != joinDto.effectiveTo) {
                        |                this.${joinRepoFieldName}.setFields(
                        |                    ${joinEntityClass}Updater.forPrimaryKey(joinId) {
                        |                        effectiveFrom(joinDto.effectiveFrom)
                        |                        effectiveTo(joinDto.effectiveTo)
                        |                    }
                        |                )
                        |            }
                        |        }
                        |""".trimMargin())
                }

                append("""
                    |
                    |    }
                    |""".trimMargin())

            } else if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType != EffectiveRangeDateType.TIMESTAMP) {

                addImportFor(Fqcns.MAIA_DOMAIN_ID)
                addImportFor(manyToManyEntityDef.entityDef.entityFqcn)

                append("""
                    |
                    |
                    |    private fun reconcile${joinNamePrefix}Joins(
                    |        id: DomainId,
                    |        submittedIds: List<DomainId>
                    |    ) {
                    |
                    |        val existing = this.${joinRepoFieldName}.findBy${thisSideFieldNameCapitalized}(id)
                    |        val existingIds = existing.map { it.${otherSideFieldName} }.toSet()
                    |        val desiredIds = submittedIds.toSet()
                    |
                    |        existing.filter { it.${otherSideFieldName} !in desiredIds }.forEach {
                    |            this.${joinRepoFieldName}.deleteByPrimaryKey(it.id)
                    |        }
                    |
                    |        val newJoins = (desiredIds - existingIds).map { $otherSideFieldName ->
                    |            ${joinEntityClass}.newInstance(${newInstanceArgsSingleLine(thisSideFieldName to "id", otherSideFieldName to otherSideFieldName)})
                    |        }
                    |
                    |        this.${joinRepoFieldName}.bulkInsert(newJoins)
                    |
                    |    }
                    |""".trimMargin())

            }

        }

    }

    private fun `render the inline update functions`() {

        this.entityDef.entityCrudApiDef?.updateApiDef?.let { apiDef ->
            apiDef.inlineEditDtoDefs.forEach { `render inline update function`(it) }
        }

    }


    private fun `render inline update function`(dtoDef: InlineEditDtoDef) {

        addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)

        val dtoUqcn = dtoDef.uqcn
        val fieldName = dtoDef.fieldDef.classFieldDef.classFieldName

        // TODO if the entity has a lastModifiedByUsername field

        blankLine()
        blankLine()
        appendPreAuthorize(this.entityDef.entityCrudApiDef?.updateApiDef?.crudApiDef?.authorityDef)
        appendLine("    fun update${fieldName.firstToUpper()}(editDto: $dtoUqcn) {")
        blankLine()
        appendLine("        val currentUsername = CurrentUserHolder.currentUsername")
        blankLine()
        appendLine("        logger.info(\"BEGIN: update${fieldName.firstToUpper()}. currentUsername=\${currentUsername}, dto=\$editDto\")")

        if (this.entityDef.versioned.value) {

            blankLine()
            appendLine("        val version = editDto.version")
            blankLine()
            appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forPrimaryKey($primaryKeyEditDtoFieldNamesCsv, version) {")

        } else {

            blankLine()
            appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forPrimaryKey($primaryKeyEditDtoFieldNamesCsv) {")

        }

        appendLine("            $fieldName(editDto.$fieldName)")

        if (entityDef.hasLastModifiedByIdField) {
            addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)
            appendLine("            lastModifiedBy(CurrentUserHolder.userId)")
        }

        if (entityDef.hasLastModifiedByUsernameField) {
            addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)
            appendLine("            lastModifiedByUsername(CurrentUserHolder.currentUsername)")
        }

        if (entityDef.hasLastModifiedTimestampUtcField) {
            addImportFor(FieldTypes.instant)
            appendLine("            lastModifiedTimestampUtc(Instant.now())")
        }

        appendLine("        }")
        blankLine()
        appendLine("        setFields(updater)")
        blankLine()
        appendLine("    }")

    }


    private fun `render the setFields function`() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        val primaryKeyFieldNamesCsv = if (this.entityDef.hasCompositePrimaryKey) {
            this.entityDef.primaryKeyClassFields.joinToString(", ") { "updater.primaryKey.${it.classFieldName.value}" }
        } else {
            this.entityDef.primaryKeyClassFields.joinToString(", ") { "updater.${it.classFieldName.value}" }
        }


        append("""
            |
            |
            |    fun setFields(updater: ${this.entityDef.entityBaseName}EntityUpdater): Int {
            |        
            |        val count = this.entityRepo.setFields(updater)
            |""".trimMargin()
        )

        if (this.entityDef.crudDef.withCrudListener.value) {
            appendLine("        this.${this.entityDef.crudNotifierClassDef.uqcn.firstToLower()}.onEntityUpdated($primaryKeyFieldNamesCsv)")
        }

        append("""
            |        return count
            |        
            |    }
            |""".trimMargin()
        )

    }


    private fun `render the delete function`() {

        if (this.entityDef.isNotDeletable) {
            return
        }

        blankLine()
        blankLine()
        appendPreAuthorize(this.entityDef.entityCrudApiDef?.deleteApiDef?.crudApiDef?.authorityDef)
        appendLine("    fun delete($primaryKeyFieldNamesAndTypesCsv) {")

        val referencingEntities = this.applicationModelDef.entitiesThatReference(this.entityDef)

        referencingEntities.forEach { referencingEntityDef ->

            val daoName = referencingEntityDef.entityRepoFqcn.uqcn.firstToLower()

            val field = referencingEntityDef.allForeignKeyEntityFieldDefs.find { it.foreignKeyFieldDef?.foreignEntityBaseName == this.entityDef.entityBaseName }

            val fieldName = field!!.classFieldName.firstToUpper()

            val isSystemManagedRef = referencingEntityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
                && referencingEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP

            blankLine()
            if (isSystemManagedRef) {
                appendLine("        if (this.${daoName}.findEffectiveBy$fieldName($primaryKeyFieldNamesCsv).isNotEmpty()) {")
            } else {
                appendLine("        if (this.${daoName}.existsBy$fieldName($primaryKeyFieldNamesCsv)) {")
            }
            appendLine("            throw this.maiaProblems.foreignKeyRecordsExist(\"${referencingEntityDef.entityBaseName}\")")
            appendLine("        }")

            if (isSystemManagedRef) {
                blankLine()
                appendLine("        this.${daoName}.findBy${fieldName}($primaryKeyFieldNamesCsv).forEach {")
                appendLine("            this.${daoName}.deleteByPrimaryKey(it.id)")
                appendLine("        }")
            }

        }

        if (this.entityDef.crudDef.withCrudListener.value) {

            blankLine()
            appendLine("        val entityToDelete = this.entityRepo.findByPrimaryKeyOrNull($primaryKeyFieldNamesCsv)")
            appendLine("                ?: return")

        }

        blankLine()
        appendLine("        this.entityRepo.deleteByPrimaryKey($primaryKeyFieldNamesCsv)")

        if (this.entityDef.crudDef.withCrudListener.value) {
            appendLine("        this.${this.entityDef.crudNotifierClassDef.uqcn.firstToLower()}.onEntityDeleted(entityToDelete)")
        }

        blankLine()
        appendLine("    }")

    }


    private fun appendPreAuthorize(authority: AuthorityDef?) {

        authority?.let {
            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
            appendLine("    @PreAuthorize(\"hasAuthority('${it.name}')\")")
        }

    }


    private fun renderNewInstanceArgsMultiLine(
        indentSize: Int,
        args: List<Pair<String, String>>
    ) {

        val indent = " ".repeat(indentSize)

        val sortedArgs = args.sortedBy { it.first }

        sortedArgs.forEachIndexed { index, (name, value) ->
            val suffix = if (index < sortedArgs.lastIndex) "," else ""
            appendLine("$indent$name = $value$suffix")
        }

    }


    private fun renderNewInstanceArgsMultiLine(
        indentSize: Int,
        vararg args: Pair<String, String>
    ) {
        renderNewInstanceArgsMultiLine(indentSize, args.toList())
    }


    private fun newInstanceArgsSingleLine(vararg args: Pair<String, String>): String {

        return args.sortedBy { it.first }.joinToString(", ") { (name, value) -> "$name = $value" }

    }


}
