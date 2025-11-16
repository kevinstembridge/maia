package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.InlineEditDtoDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
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
    private val modelDef: ModelDef
) : AbstractKotlinRenderer(
    entityDef.crudServiceClassDef
) {


    private val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(entityDef.primaryKeyClassFields)


    private val primaryKeyFieldNamesCsv = fieldNamesCsv(entityDef.primaryKeyClassFields)


    private val primaryKeyEditDtoFieldNamesCsv = entityDef.primaryKeyClassFields.joinToString(", ") {
        "editDto.${it.classFieldName.value}"
    }


    init {

        addConstructorArg(ClassFieldDef.aClassField("entityRepo", this.entityDef.entityRepoFqcn).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("maiaProblems", Fqcns.MAIA_PROBLEMS).privat().build())

        this.modelDef.entitiesThatReference(this.entityDef).forEach { referencingEntityDef ->
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

        `render create by API`()
        `render create`()
        `render update function`()
        `render inline update functions`()
        `render setFields function`()
        `render delete function`()

    }


    private fun `render create`() {

        val createApiDef = this.entityDef.entityCrudApiDef?.createApiDef

        val createContext = createApiDef?.crudApiDef?.context

        blankLine()
        blankLine()

        if (createContext != null) {
            appendLine("    fun create(entity: ${this.entityDef.entityUqcn}, context: ${createContext.uqcn}): ${this.entityDef.entityUqcn} {")
        } else {
            appendLine("    fun create(entity: ${this.entityDef.entityUqcn}): ${this.entityDef.entityUqcn} {")
        }

        blankLine()
        appendLine("        this.entityRepo.insert(entity)")

        if (this.entityDef.crudDef.withCrudListener.value) {

            if (createContext == null) {
                appendLine("        this.${this.entityDef.crudNotifierClassDef.uqcn.firstToLower()}.onEntityCreated(entity)")
            } else {
                appendLine("        this.${this.entityDef.crudNotifierClassDef.uqcn.firstToLower()}.onEntityCreated(entity, context)")
            }

        }

        appendLine("        return entity")
        blankLine()
        appendLine("    }")

    }



    private fun `render create by API`() {

        val createApiDef = this.entityDef.entityCrudApiDef?.createApiDef ?: return

        blankLine()
        blankLine()
        appendLine("    fun create(createDto: ${createApiDef.requestDtoDef.uqcn}): ${this.entityDef.entityUqcn} {")

        if (this.entityDef.hasCreatedByIdField || this.entityDef.hasCreatedByField) {

            addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)

            blankLine()
            appendLine("        val currentUser = CurrentUserHolder.currentUser")
            blankLine()
            appendLine("        logger.info(\"BEGIN: create ${this.entityDef.entityBaseName}. createdBy=\${currentUser.username}, dto=\$createDto\")")

        } else {

            blankLine()
            appendLine("        logger.info(\"BEGIN: create ${this.entityDef.entityBaseName}. dto=\$createDto\")")

        }

        val currentUserOrBlank = if (this.entityDef.hasCreatedByIdField || this.entityDef.hasLastModifiedByIdField) {
            ", currentUser"
        } else {
            ""
        }

        blankLine()
        appendLine("        val entity: ${this.entityDef.entityUqcn} = buildEntity(createDto${currentUserOrBlank})")

        if (createApiDef.crudApiDef.context != null) {
            appendLine("        return create(entity, createDto.context)")
        } else {
            appendLine("        return create(entity)")
        }

        blankLine()
        appendLine("    }")

        `render function buildEntity`(createApiDef)

    }


    private fun `render function buildEntity`(apiDef: EntityCreateApiDef) {

        addImportFor<Instant>()

        val currentUserOrBlank = if (this.entityDef.hasCreatedByIdField || this.entityDef.hasLastModifiedByIdField) {
            addImportFor(Fqcns.MAIA_USER_DETAILS)
            ", currentUser: MaiaUserDetails"
        } else {
            ""
        }

        blankLine()
        blankLine()
        appendLine("    private fun buildEntity(createDto: ${apiDef.requestDtoDef.uqcn}$currentUserOrBlank): ${this.entityDef.entityUqcn} {")
        blankLine()

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

            appendLine("        val createdById = currentUser.userId")

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
            appendLine("        val lastModifiedById = currentUser.userId")
        }

        if (this.entityDef.hasLastModifiedByUsernameField) {
            appendLine("        val lastModifiedByUsername = currentUser.username")
        }

        if (this.entityDef.hasLastModifiedTimestampUtcField) {
            appendLine("        val lastModifiedTimestampUtc = createdTimestampUtc")
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
            is IdAndNameFieldType -> TODO("YAGNI?")
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


    private fun `render update function`() {

        val apiDef = this.entityDef.entityCrudApiDef?.updateApiDef
            ?: return

        addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)
        apiDef.requestDtoDef.let { dtoDef ->

            blankLine()
            blankLine()
            appendLine("    fun update(editDto: ${dtoDef.uqcn}) {")
            blankLine()
            appendLine("        val id = editDto.id")

            if (this.entityDef.versioned.value) {

                appendLine("        val version = editDto.version")
                appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forPrimaryKey($primaryKeyFieldNamesCsv, version) {")

            } else {
                appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forPrimaryKey($primaryKeyFieldNamesCsv) {")
            }

            dtoDef.classFieldDefs
                .filter { it.classFieldName != ClassFieldName.id && it.classFieldName != ClassFieldName.version }
                .forEach { field -> appendLine("            ${field.classFieldName}(editDto.${field.classFieldName})") }

            if (this.entityDef.hasLastModifiedByIdField) {
                addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)
                appendLine("            lastModifiedById(CurrentUserHolder.userId)")
            }

            if (this.entityDef.hasLastModifiedTimestampUtcField) {
                addImportFor<Instant>()
                appendLine("            lastModifiedTimestampUtc(Instant.now())")
            }

            appendLine("        }.build()")
            blankLine()
            appendLine("        setFields(updater)")

        }

        blankLine()
        appendLine("    }")

    }

    private fun `render inline update functions`() {

        this.entityDef.entityCrudApiDef?.updateApiDef?.let { apiDef ->
            apiDef.inlineEditDtoDefs.forEach { `render inline update function`(it) }
        }

    }


    private fun `render inline update function`(dtoDef: InlineEditDtoDef) {

        addImportFor(Fqcns.MAIA_CURRENT_USER_HOLDER)

        val dtoUqcn = dtoDef.uqcn
        val fieldName = dtoDef.fieldDef.classFieldDef.classFieldName

        blankLine()
        blankLine()
        appendLine("    fun update${fieldName.firstToUpper()}(editDto: $dtoUqcn) {")
        blankLine()
        appendLine("        val currentUser = CurrentUserHolder.currentUser")
        blankLine()
        appendLine("        logger.info(\"BEGIN: update${fieldName.firstToUpper()}. currentUser=\${currentUser.username}, dto=\$editDto\")")

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
        appendLine("        }.build()")
        blankLine()
        appendLine("        setFields(updater)")
        blankLine()
        appendLine("    }")

    }


    private fun `render setFields function`() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        appendLine("""
            |
            |
            |    fun setFields(updater: ${this.entityDef.entityBaseName}EntityUpdater): Int {
            |        
            |        val count = this.entityRepo.setFields(updater)""".trimMargin()
        )

        if (this.entityDef.crudDef.withCrudListener.value) {
            appendLine("        this.${this.entityDef.crudNotifierClassDef.uqcn.firstToLower()}.onEntityUpdated(updater.id)")
        }

        appendLine("""
            |        return count
            |        
            |    }""".trimMargin()
        )

    }


    private fun `render delete function`() {

        if (this.entityDef.isNotDeletable) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun delete($primaryKeyFieldNamesAndTypesCsv) {")

        val referencingEntities = this.modelDef.entitiesThatReference(this.entityDef)

        referencingEntities.forEach { referencingEntityDef ->

            val daoName = referencingEntityDef.entityRepoFqcn.uqcn.firstToLower()

            val field = referencingEntityDef.allForeignKeyEntityFieldDefs.find { it.foreignKeyFieldDef?.foreignEntityBaseName == this.entityDef.entityBaseName }

            val fieldName = field!!.classFieldName.firstToUpper()

            blankLine()
            appendLine("        if (this.${daoName}.existsBy$fieldName($primaryKeyFieldNamesCsv)) {")
            appendLine("            throw this.maiaProblems.foreignKeyRecordsExist(\"${referencingEntityDef.entityBaseName}\")")
            appendLine("        }")

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


}
