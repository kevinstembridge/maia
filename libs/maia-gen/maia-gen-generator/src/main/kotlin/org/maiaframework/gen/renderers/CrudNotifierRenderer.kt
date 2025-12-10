package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.InlineEditDtoDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
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

class CrudNotifierRenderer(
    private val entityDef: EntityDef,
) : AbstractKotlinRenderer(
    entityDef.crudNotifierClassDef
) {


    override fun renderPreClassFields() {

        addImportFor(Fqcns.SPRING_APPLICATION_CONTEXT)

        blankLine()
        blankLine()
        appendLine("    private lateinit var applicationContext: ApplicationContext")

        renderFieldForCrudListeners(entityDef)

        blankLine()
        blankLine()
        appendLine("    override fun setApplicationContext(applicationContext: ApplicationContext) {")
        appendLine("        this.applicationContext = applicationContext")
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    override fun afterPropertiesSet() {")
        blankLine()

        renderAssignmentOfCrudListeners(entityDef)

        blankLine()
        appendLine("    }")

    }


    private fun renderFieldForCrudListeners(someEntityDef: EntityDef) {

        if (someEntityDef.crudDef.withCrudListener.value) {
            addImportFor(someEntityDef.crudListenerClassDef.fqcn)
            val listenersCollectionFieldName = listenersCollectionFieldName(someEntityDef)
            blankLine()
            blankLine()
            appendLine("    private lateinit var ${listenersCollectionFieldName}: Map<String, ${someEntityDef.crudListenerClassDef.uqcn}>")
        }

        someEntityDef.superclassEntityDef?.let { superEntityDef ->
            if (superEntityDef.crudDef.withCrudListener.value) {
                renderFieldForCrudListeners(superEntityDef)
            }
        }

    }


    private fun renderAssignmentOfCrudListeners(someEntityDef: EntityDef) {

        if (someEntityDef.crudDef.withCrudListener.value) {
            appendLine("        this.${listenersCollectionFieldName(someEntityDef)} = this.applicationContext.getBeansOfType(${someEntityDef.crudListenerClassDef.uqcn}::class.java)")
        }

        someEntityDef.superclassEntityDef?.let { renderAssignmentOfCrudListeners(it) }

    }


    private fun listenersCollectionFieldName(someEntityDef: EntityDef) = "${someEntityDef.crudListenerClassDef.uqcn.firstToLower()}s"


    override fun renderFunctions() {

        `render function onEntityCreated`()
        `render function onEntityUpdated`()
        `render function onEntityDeleted`()

    }


    private fun `render function onEntityCreated`() {

        blankLine()
        blankLine()
        appendLine("    fun onEntityCreated(entity: ${this.entityDef.entityUqcn}) {")

        if (this.entityDef.crudDef.withCrudListener.value) {
            renderNotificationOfCreateListeners(this.entityDef)
        }

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
        blankLine()
        appendLine("        return create(entity)")
        blankLine()
        appendLine("    }")

        `render function buildEntity`(createApiDef)

    }


    private fun renderNotificationOfCreateListeners(someEntityDef: EntityDef) {

        someEntityDef.entityCrudApiDef?.createApiDef?.let { _ ->

            blankLine()
            appendLine("        this.${listenersCollectionFieldName(someEntityDef)}.forEach { (_, listener) ->")
            appendLine("            listener.on${someEntityDef.entityUqcn}Created(entity)")
            appendLine("        }")

        }

        someEntityDef.superclassEntityDef?.let { renderNotificationOfCreateListeners(it) }

    }


    private fun renderNotificationOfUpdateListeners(someEntityDef: EntityDef) {

        if (someEntityDef.crudDef.withCrudListener.value) {

            val primaryKeyFieldNamesCsv = fieldNamesCsv(someEntityDef.primaryKeyClassFields)

            blankLine()
            appendLine("        this.${listenersCollectionFieldName(someEntityDef)}.forEach { (_, listener) ->")
            appendLine("            listener.on${someEntityDef.entityUqcn}Updated($primaryKeyFieldNamesCsv)")
            appendLine("        }")

        }

        someEntityDef.superclassEntityDef?.let { renderNotificationOfUpdateListeners(it) }

    }


    private fun renderNotificationOfDeleteListeners(someEntityDef: EntityDef) {

        someEntityDef.entityCrudApiDef?.deleteApiDef?.let {

            blankLine()
            appendLine("        this.${listenersCollectionFieldName(someEntityDef)}.forEach { (_, listener) ->")
            appendLine("            listener.on${it.entityDef.entityUqcn}Deleted(entity)")
            appendLine("        }")

        }

        someEntityDef.superclassEntityDef?.let { renderNotificationOfDeleteListeners(it) }

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

        appendLine("        val id = DomainId.newId()")
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
                appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forIdAndVersion(id, version) {")

            } else {
                appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forId(id) {")
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
        blankLine()
        appendLine("        val id = editDto.id")

        if (this.entityDef.versioned.value) {

            appendLine("        val version = editDto.version")
            blankLine()
            appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forIdAndVersion(id, version) {")

        } else {

            blankLine()
            appendLine("        val updater = ${this.entityDef.entityBaseName}EntityUpdater.forId(id) {")

        }

        appendLine("            $fieldName(editDto.$fieldName)")
        appendLine("        }.build()")
        blankLine()
        appendLine("        setFields(updater)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function onEntityUpdated`() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(this.entityDef.primaryKeyClassFields)

        this.entityDef.primaryKeyClassFields.forEach { addImportFor(it.fieldType)}

        appendLine("""
            |
            |
            |    fun onEntityUpdated($primaryKeyFieldNamesAndTypesCsv) {""".trimMargin()
        )

        if (this.entityDef.crudDef.withCrudListener.value) {
            renderNotificationOfUpdateListeners(this.entityDef)
        }

        appendLine("""
            |
            |    }""".trimMargin()
        )

    }


    private fun `render function onEntityDeleted`() {

        if (this.entityDef.isNotDeletable) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun onEntityDeleted(entity: ${this.entityDef.entityUqcn}) {")

        if (this.entityDef.crudDef.withCrudListener.value) {
            renderNotificationOfDeleteListeners(this.entityDef)
        }

        blankLine()
        appendLine("    }")

    }


}
