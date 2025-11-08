package org.maiaframework.gen.spec.definition


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.domain.persist.SortDirection
import org.maiaframework.domain.types.TypeDiscriminator
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.DaoHasSpringAnnotation
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.EntityDaoHasSpringAnnotation
import org.maiaframework.gen.spec.definition.flags.HasEffectiveLocalDates
import org.maiaframework.gen.spec.definition.flags.HasEffectiveTimestamps
import org.maiaframework.gen.spec.definition.flags.HasEntityDetailDtoDef
import org.maiaframework.gen.spec.definition.flags.HasSingleEffectiveRecord
import org.maiaframework.gen.spec.definition.flags.IsCappedCollection
import org.maiaframework.gen.spec.definition.flags.IsCreatableByUser
import org.maiaframework.gen.spec.definition.flags.IsDeltaEntity
import org.maiaframework.gen.spec.definition.flags.IsDeltaField
import org.maiaframework.gen.spec.definition.flags.IsDeltaKey
import org.maiaframework.gen.spec.definition.flags.IsDerived
import org.maiaframework.gen.spec.definition.flags.IsPrimaryKey
import org.maiaframework.gen.spec.definition.flags.Versioned
import org.maiaframework.gen.spec.definition.flags.WithHandCodedDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEntityDao
import org.maiaframework.gen.spec.definition.flags.WithVersionHistory
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.Uqcn
import org.maiaframework.jdbc.JdbcCompatibleType
import org.maiaframework.jdbc.TableName
import java.util.Objects


class EntityDef(
    val entityBaseName: EntityBaseName,
    tableNameOrNull: TableName?,
    val schemaName: SchemaName,
    val databaseType: DatabaseType,
    val configurableSchemaPropertyName: ConfigurableSchemaPropertyName?,
    val isCappedCollection: IsCappedCollection,
    val cappedSizeInBytes: Long?,
    val packageName: PackageName,
    val description: Description?,
    val moduleName: ModuleName?,
    val isDeltaEntity: IsDeltaEntity,
    val entityFieldsNotInherited: List<EntityFieldDef>,
    val superclassEntityDef: EntityDef?,
    private val isAbstract: Boolean,
    val typeDiscriminatorOrNull: TypeDiscriminator?,
    withHandCodedDao: WithHandCodedDao,
    withHandCodedEntityDao: WithHandCodedEntityDao,
    val deletable: Deletable,
    val daoHasSpringAnnotation: DaoHasSpringAnnotation,
    val entityDaoHasSpringAnnotation: EntityDaoHasSpringAnnotation,
    val allowDeleteAll: AllowDeleteAll,
    val allowFindAll: AllowFindAll,
    private val providedIndexDefs: List<IndexDef>,
    val crudDef: CrudDef,
    val withVersionHistory: WithVersionHistory,
    val versioned: Versioned,
    val isHistoryEntity: Boolean,
    /**
     * If an entity is referenced by a foreign key field in some other entity, then when
     * we create or edit instances of that entity we will need to display a dropdown field
     * that allows the user to select a value for the foreign entity. That dropdown will
     * be backed by the id of the entity, and it will show a display name. The code
     * generator needs to know the name of the entity field that will be used as the
     * display name in those dropdowns.
     */
    private val nameFieldForIdAndNameDto: String? = null,
    val stagingEntityFieldDefs: List<DataRowStagingEntityFieldDef> = emptyList(),
    val hasEffectiveTimestamps: HasEffectiveTimestamps,
    val hasEffectiveLocalDates: HasEffectiveLocalDates,
    val hasSingleEffectiveRecord: HasSingleEffectiveRecord,
    private val hasEntityDetailDtoDef: HasEntityDetailDtoDef,
    val cacheableDef: CacheableDef?
) {


    val entityUqcn = Uqcn("${entityBaseName}Entity")


    val entityCacheName = entityUqcn.toSnakeCase()


    val entityFqcn = packageName.uqcn(entityUqcn)


    val tableName: TableName = initTableName(tableNameOrNull)


    val schemaAndTableName: String = if (this.configurableSchemaPropertyName == null) {
        "${schemaName}.${tableName}".lowercase()
    } else {
        "\$schemaName.${tableName.value.lowercase()}"
    }


    val superEntityBaseName: EntityBaseName = superclassEntityDef?.superEntityBaseName ?: entityBaseName


    val entityClassDef = aClassDef(this.entityFqcn)
        .withAbstract(isAbstract)
        .withFieldDefsNotInherited(this.entityFieldsNotInherited.map { it.classFieldDef })
        .withSuperclass(initSuperclassDef(superclassEntityDef))
        .build()


    val metaClassDef = aClassDef(this.entityFqcn.withSuffix("Meta"))
        .ofType(ClassType.OBJECT)
        .build()


    val stagingEntityExtensionsClassDef = aClassDef(this.entityFqcn.withSuffix("Extensions"))
        .ofType(ClassType.OBJECT)
        .build()


    val entityFilterClassDef = aClassDef(this.entityFqcn.withSuffix("Filter"))
        .ofType(ClassType.INTERFACE)
        .build()


    val entityFiltersClassDef = aClassDef(this.entityFqcn.withSuffix("Filters"))
        .ofType(ClassType.CLASS)
        .build()


    val entityUpdaterClassDef = aClassDef(this.entityFqcn.withSuffix("Updater"))
        .ofType(ClassType.DATA_CLASS)
        .withSuperclass(initEntityUpdaterSuperclassDef())
        .build()


    val entityFieldConverterClassDef = aClassDef(this.entityFqcn.withSuffix("FieldConverter"))
        .withInterface(ParameterizedType(Fqcns.ENTITY_FIELD_CONVERTER))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val foreignKeyReferencesEndpointClassDef =
        aClassDef(packageName.uqcn("${entityBaseName}CheckForeignKeyReferencesEndpoint"))
            .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
            .build()


    val foreignKeyReferencesServiceClassDef =
        aClassDef(packageName.uqcn("${entityBaseName}ForeignKeyReferencesService"))
            .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
            .build()


    val daoFqcn: Fqcn = packageName.uqcn("${entityBaseName}Dao")


    val entityDaoFqcn: Fqcn = packageName.uqcn("${entityBaseName}Dao")


    val repoFqcn: Fqcn = packageName.uqcn("${entityBaseName}Repo")


    val entityRepoFqcn: Fqcn = packageName.uqcn("${entityBaseName}Repo")


    val entityRepoClassDef = aClassDef(entityRepoFqcn)
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val fetchForEditDtoFqcn = packageName.uqcn("${entityBaseName}FetchForEditDto")


    private val rowMapperFqcn = packageName.uqcn("${entityBaseName}EntityRowMapper")


    private val fetchForEditDtoRowMapperFqcn = fetchForEditDtoFqcn.withSuffix("RowMapper")


    val daoIndexCreatorClassDef = aClassDef(this.daoFqcn.withSuffix("IndexCreator"))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val entityDaoClassDefToRender: ClassDef


    val daoClassDefToRender: ClassDef


    private val entityFieldsInherited: List<EntityFieldDef> = this.superclassEntityDef?.allEntityFields.orEmpty()


    val allEntityFields: List<EntityFieldDef> = listOf(this.entityFieldsNotInherited, entityFieldsInherited).flatten()


    val allEntityFieldsSorted: List<EntityFieldDef> = allEntityFields.sorted()


    val allClassFields: List<ClassFieldDef> = allEntityFields
        .asSequence()
        .map { it.classFieldDef }
        .toList()


    val allClassFieldsSorted: List<ClassFieldDef> = allClassFields.sorted()


    val hasAnyMatSelectFields = allEntityFields.any { it.fieldType is EnumFieldType }


    val enumsForMatSelectFields = allEntityFields.filter { it.fieldType is EnumFieldType }.map { (it.fieldType as EnumFieldType).enumDef }


    val typeDiscriminator: TypeDiscriminator
        get() = this.typeDiscriminatorOrNull
            ?: throw RuntimeException("Expected entity to have a type discriminator: ${this.entityBaseName}")


    val isRootEntity = this.superclassEntityDef == null


    val isConcrete: Boolean = this.isAbstract == false


    val hasIdAndNameDtoDef: Boolean
        get() = this.nameFieldForIdAndNameDto != null


    val entityIdAndNameDef: EntityIdAndNameDef
        get() {

            requireNotNull(this.nameFieldForIdAndNameDto) { "nameFieldForIdAndNameDto for entity $entityBaseName must not be null if idAndNameDef is required." }

            val idEntityFieldDef = findFieldByName("id")
            val nameEntityFieldDef = findFieldByName(this.nameFieldForIdAndNameDto)

            return EntityIdAndNameDef(
                packageName,
                DtoBaseName(entityBaseName.value),
                idEntityFieldDef,
                nameEntityFieldDef,
                entityRepoClassDef
            )

        }


    val fetchForEditDtoDef = if (crudDef.crudApiDefs.updateApiDef != null) {
        FetchForEditDtoDef(
            packageName,
            entityBaseName,
            allEntityFieldsSorted,
            databaseType
        )
    } else {
        null
    }


    val hazelcastSerializerClassDef = aClassDef(packageName.uqcn(entityBaseName.withSuffix("Serializer").value))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedTypes.hazelcastCompactSerializer(ParameterizedType(entityFqcn)))
        .build()


    val primaryKeyFields: List<EntityFieldDef> = allEntityFields.filter { it.isPrimaryKey.value }


    val hasSurrogatePrimaryKey = primaryKeyFields.size == 1
            && primaryKeyFields.first().classFieldName == ClassFieldName.id
            && primaryKeyFields.first().fieldType is DomainIdFieldType


    val versionField: EntityFieldDef? = findFieldByNameOrNull(ClassFieldName.version.value)


    val isStagingEntity: Boolean = this.stagingEntityFieldDefs.isNotEmpty()


    val allTypeaheadFields: List<EntityFieldDef>
        get() = allEntityFields.filter { it.typeaheadDef != null }


    val allForeignKeyEntityFieldDefs: List<EntityFieldDef>
        get() = allEntityFields.filter { it.fieldType is ForeignKeyFieldType }


    val allTypeaheadDefs: List<TypeaheadDef>
        get() = allEntityFields.mapNotNull { it.typeaheadDef }


    val allDeltaKeyFields: List<EntityFieldDef>
        get() = allEntityFields.filter { it.isDeltaKey.value }


    val allDeltaFields: List<EntityFieldDef>
        get() = allEntityFields.filter { it.isDeltaField.value }


    val allModifiableFieldDef: List<EntityFieldDef>
        get() = allEntityFields.filter { it.classFieldDef.isModifiableBySystem || it.classFieldDef.isEditableByUser.value }


    val allFieldsForEntityUpdaters: List<EntityFieldDef>
        get() = allEntityFields
            .filter { it.classFieldDef.isModifiableBySystem || it.classFieldDef.isEditableByUser.value }
            .filterNot { it.classFieldDef.classFieldName == ClassFieldName.version }


    val allUnmodifiableFieldDef: List<EntityFieldDef>
        get() = allEntityFields.filter { fieldDef -> (fieldDef.classFieldDef.isModifiableBySystem == false && fieldDef.classFieldDef.isEditableByUser.value == false) }


    val allUserEditableFields: List<EntityFieldDef>
        get() = allEntityFields.filter { it.classFieldDef.isEditableByUser.value }


    /**
     * These are the fields that must be provided by the user who is creating the new record.
     * It does not include those fields that must be determined on the back end.
     */
    val allFieldsRequiredInCreateRequest: List<EntityFieldDef>
        get() = allEntityFieldsSorted
            .asSequence()
            .filterNot(byName(ClassFieldName.id))
            .filterNot(byName(ClassFieldName.createdById))
            .filterNot(byName(ClassFieldName.createdByUsername))
            .filterNot(byName(ClassFieldName.createdTimestampUtc))
            .filterNot(byName(ClassFieldName.lastModifiedById))
            .filterNot(byName(ClassFieldName.lastModifiedByUsername))
            .filterNot(byName(ClassFieldName.lastModifiedTimestampUtc))
            .filterNot(byName(ClassFieldName.version))
            .toList()


    val isNotDeletable: Boolean = this.deletable.value == false


    val isModifiable: Boolean = this.entityClassDef.isModifiable


    val historyEntityDef: EntityDef? = if (withVersionHistory.value) {

        val historyEntityBaseName = entityBaseName.withSuffix("History")
        val historyTableName = tableName.withSuffix("_history")

        val historyFieldDefs = entityFieldsNotInherited
            .asSequence()
            .map { fd ->

                val isPrimaryKey = if (
                    fd.isPrimaryKey.value
                    || fd.classFieldDef.classFieldName == ClassFieldName.version
                ) {
                    IsPrimaryKey.TRUE
                } else {
                    IsPrimaryKey.FALSE
                }

                EntityFieldDef(
                    historyEntityBaseName,
                    packageName,
                    fd.classFieldDef.convertToUnmodifiable().convertToNotUnique(),
                    fd.dbColumnFieldDef.tableColumnName,
                    withExistsEndpoint = false,
                    IsDeltaKey.FALSE,
                    isPrimaryKey,
                    IsDeltaField.TRUE,
                    fd.isDerived,
                    isCreatableByUser = IsCreatableByUser.FALSE,
                    fd.dbColumnFieldDef.fieldReaderParameterizedType,
                    fd.dbColumnFieldDef.fieldWriterParameterizedType
                )

            }.plus(if (isRootEntity) changeTypeFieldDef(historyEntityBaseName, packageName) else null)
            .filterNotNull()
            .toList()

        val historyEntityIndexDefs = providedIndexDefs.map { it.asNonUnique().withNamePrefix("hist") }

        EntityDef(
            allowDeleteAll = AllowDeleteAll.FALSE,
            allowFindAll = AllowFindAll.FALSE,
            cacheableDef = null,
            cappedSizeInBytes = null,
            tableNameOrNull = historyTableName,
            configurableSchemaPropertyName = null,
            crudDef = CrudDef.EMPTY,
            daoHasSpringAnnotation = this.daoHasSpringAnnotation,
            databaseType = this.databaseType,
            deletable = Deletable.FALSE,
            description = null,
            entityBaseName = historyEntityBaseName,
            entityDaoHasSpringAnnotation = this.entityDaoHasSpringAnnotation,
            entityFieldsNotInherited = historyFieldDefs,
            hasEffectiveLocalDates = this.hasEffectiveLocalDates,
            hasEffectiveTimestamps = this.hasEffectiveTimestamps,
            hasEntityDetailDtoDef = this.hasEntityDetailDtoDef,
            hasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
            isAbstract = this.isAbstract,
            isCappedCollection = IsCappedCollection.FALSE,
            isDeltaEntity = IsDeltaEntity.FALSE,
            isHistoryEntity = true,
            moduleName = this.moduleName,
            packageName = this.packageName,
            providedIndexDefs = historyEntityIndexDefs,
            schemaName = this.schemaName,
            stagingEntityFieldDefs = emptyList(),
            superclassEntityDef = this.superclassEntityDef?.historyEntityDef,
            typeDiscriminatorOrNull = this.typeDiscriminatorOrNull,
            versioned = Versioned.FALSE,
            withHandCodedDao = WithHandCodedDao.FALSE,
            withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
            withVersionHistory = WithVersionHistory.FALSE
        )

    } else {
        null
    }


    private val allIndexDefs = providedIndexDefs //initIndexDefs()


    private fun initIndexDefs(): List<IndexDef> {

        return if (versioned.value) {

            val idField = findFieldByName(ClassFieldName.id.value)

            val idAndVersionIndexFieldDefs = listOf(
                IndexFieldDef(idField, SortDirection.desc),
                IndexFieldDef(versionFieldDef(this.entityBaseName, this.packageName), SortDirection.desc)
            )

            val idAndVersionIndexDef = IndexDef(entityBaseName, null, idAndVersionIndexFieldDefs, isUnique = true, isSparse = false)

            providedIndexDefs.plus(idAndVersionIndexDef)

        } else {

            providedIndexDefs

        }

    }


    val databaseIndexDefs: List<DatabaseIndexDef>
        get() = allIndexDefs.map { DatabaseIndexDef(
            it,
            this.packageName,
            DatabaseIndexBaseName(this.entityBaseName.value),
            this.moduleName,
            crudAngularComponentNames.serviceName
        ) }


    val uniqueIndexDefs: List<DatabaseIndexDef>
        get() = databaseIndexDefs.filter { it.isUnique }


    val multiFieldUniqueIndexDefs: List<DatabaseIndexDef>
        get() = allIndexDefs
            .filter { it.isMultiField }
            .filter { it.isUnique }
            .filter { it.isForIdAndVersion == false }
            .map { DatabaseIndexDef(
                it,
                this.packageName,
                DatabaseIndexBaseName(this.entityBaseName.value),
                this.moduleName,
                crudAngularComponentNames.serviceName
            ) }


    val rowMapperClassDef = aClassDef(this.rowMapperFqcn)
        .withInterface(ParameterizedType(Fqcns.MAHANA_JDBC_ROW_MAPPER, ParameterizedType(this.entityFqcn)))
        .build()


    val fetchForEditDtoRowMapperClassDef = aClassDef(this.fetchForEditDtoRowMapperFqcn)
        .withInterface(ParameterizedType(Fqcns.MAHANA_JDBC_ROW_MAPPER, ParameterizedType(this.fetchForEditDtoFqcn)))
        .build()


    val entityDetailDtoDef: EntityDetailDtoDef? by lazy {
        if (hasEntityDetailDtoDef.value) {
            EntityDetailDtoDef(
                this
            )
        } else {
            null
        }
    }


    val crudAngularComponentNames = AngularComponentNames(this.packageName, "${this.entityBaseName}Crud")


    val crudServiceClassDef: ClassDef


    val crudNotifierClassDef: ClassDef


    val crudEndpointClassDef = aClassDef(packageName.uqcn("${entityBaseName}CrudEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val crudListenerClassDef = aClassDef(packageName.uqcn("${entityBaseName}CrudListener"))
        .ofType(ClassType.INTERFACE)
        .build()


    val entityCrudApiDef = initCrudApiDef()


    private fun initCrudApiDef(): EntityCrudApiDef? {

        val createApiDef = this.crudDef.crudApiDefs.createApiDef?.let { EntityCreateApiDef(this, it, this.moduleName) }

        val updateApiDef = this.crudDef.crudApiDefs.updateApiDef?.let {
            if (this.entityClassDef.isModifiable == false && this.isAbstract == false) {
                throw RuntimeException("Entity $entityBaseName is declared with a Update API but has no modifiable fields.")
            }
            EntityUpdateApiDef(this, it, this.moduleName)
        }

        val deleteApiDef = this.crudDef.crudApiDefs.deleteApiDef?.let {
            if (this.deletable.value == false) {
                throw RuntimeException("Entity $entityBaseName is declared with a Delete API but is not marked as deletable.")
            }
            EntityDeleteApiDef(this, it, this.moduleName)
        }

        val entityCrudApiDef = EntityCrudApiDef(
            this,
            createApiDef,
            updateApiDef,
            deleteApiDef,
            this.crudDef.crudApiDefs.superclassCrudApiDef
        )

        return if (entityCrudApiDef.isEmpty) {
            null
        } else {
            entityCrudApiDef
        }

    }


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val checkForeignKeyReferencesDialog =
        AngularComponentNames(packageName, "${entityBaseName}CheckForeignKeyReferencesDialog")


    val checkForeignKeyReferencesEndpointUrl =
        "/api/$modulePath${entityBaseName.toSnakeCase()}/check_foreign_references"


    init {

        nameFieldForIdAndNameDto?.let { findFieldByNameOrNull(it)
            ?: throw IllegalStateException("nameFieldForIdAndNameDto references a non-existent field $nameFieldForIdAndNameDto on entity $entityBaseName")
        }

        val abstractDaoSuperclassDef =
            initDaoSuperclassDef(packageName.uqcn(this.entityUqcn), this.historyEntityDef?.entityClassDef?.fqcn)

        if (withHandCodedDao.value) {
            this.daoClassDefToRender = aClassDef(daoFqcn.withPrefix("Abstract"))
                .withAbstract(true)
                .withSuperclass(abstractDaoSuperclassDef)
                .build()

        } else {

            val classDefBuilder = aClassDef(daoFqcn)

            if (daoHasSpringAnnotation.value) {
                classDefBuilder.withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
            }

            this.daoClassDefToRender = classDefBuilder
                .withSuperclass(abstractDaoSuperclassDef)
                .build()

        }

        if (withHandCodedEntityDao.value) {
            this.entityDaoClassDefToRender = aClassDef(entityDaoFqcn.withPrefix("Abstract"))
                .withAbstract(true)
                .withSuperclass(abstractDaoSuperclassDef)
                .build()

        } else {

            val classDefBuilder = aClassDef(entityDaoFqcn)

            if (entityDaoHasSpringAnnotation.value) {
                classDefBuilder.withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
            }

            this.entityDaoClassDefToRender = classDefBuilder
                .withSuperclass(abstractDaoSuperclassDef)
                .build()

        }

        val crudServiceClassDefBuilder = aClassDef(packageName.uqcn(crudAngularComponentNames.serviceName))
            .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)

        this.crudServiceClassDef = crudServiceClassDefBuilder.build()

        val crudNotifierClassDefBuilder = aClassDef(packageName.uqcn(crudAngularComponentNames.notifierName))
            .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)

        if (this.crudDef.withCrudListener.value) {
            crudNotifierClassDefBuilder.withInterfaces(
                ParameterizedTypes.SPRING_APPLICATION_CONTEXT_AWARE,
                ParameterizedTypes.SPRING_INITIALIZING_BEAN
            )
        }

        this.crudNotifierClassDef = crudNotifierClassDefBuilder.build()

    }


    private fun initDaoSuperclassDef(entityFqcn: Fqcn, historyEntityFqcn: Fqcn?): ClassDef? {

        return when (this.databaseType) {

            DatabaseType.JDBC -> null

            DatabaseType.MONGO -> {

                val abstractDaoParameterizedType = when (this.withVersionHistory.value) {
                    true -> ParameterizedType(
                        Fqcns.ABSTRACT_ENTITY_WITH_HISTORY_MONGO_DAO,
                        ParameterizedType(entityFqcn),
                        ParameterizedType(historyEntityFqcn!!)
                    )

                    false -> ParameterizedType(Fqcns.ABSTRACT_ENTITY_MONGO_DAO, ParameterizedType(entityFqcn))
                }

                aClassDef(abstractDaoParameterizedType).build()

            }

        }

    }


    private fun initEntityUpdaterSuperclassDef(): ClassDef? {

        return when (databaseType) {
            DatabaseType.MONGO -> {
                aClassDef(ParameterizedType(Fqcns.ABSTRACT_ENTITY_UPDATER)).build()
            }

            else -> {
                null
            }
        }

    }


    private fun superclassHasNoLastModifiedTimestampUtcField(): Boolean {

        return superclassEntityDef?.hasNoFieldNamed(ClassFieldName.lastModifiedTimestampUtc) ?: true

    }


    private fun superclassHasNoLastModifiedByIdField(): Boolean {

        return superclassEntityDef?.hasNoFieldNamed(ClassFieldName.lastModifiedById) ?: true

    }


    private fun superclassHasNoCreatedByIdField(): Boolean {

        return superclassEntityDef?.hasNoFieldNamed(ClassFieldName.createdById) ?: true

    }


    private fun initTableName(providedTableName: TableName?): TableName {

        return if (superclassIsAbstractEntity()) {

            providedTableName ?: TableName(this.entityBaseName.toSnakeCase())

        } else {

            this.superclassEntityDef?.tableName
                ?: providedTableName
                ?: TableName(this.entityBaseName.toSnakeCase())

        }

    }


    private fun superclassIsAbstractEntity(): Boolean {

        return this.superclassEntityDef?.let { entityDef ->
            entityDef.entityBaseName == ABSTRACT_ENTITY_BASE_NAME
                    || entityDef.entityBaseName == ABSTRACT_ENTITY_HISTORY_BASE_NAME
                    || entityDef.entityBaseName == ABSTRACT_VERSIONED_ENTITY_BASE_NAME
        }
            ?: false

    }


    private fun initSuperclassDef(superclassEntityDef: EntityDef?): ClassDef? {

        return superclassEntityDef?.entityClassDef

    }


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val entityDef = other as EntityDef?
        return entityBaseName == entityDef!!.entityBaseName

    }


    override fun hashCode(): Int {

        return Objects.hash(entityBaseName)

    }


    override fun toString(): String {

        return "EntityDef{" + this.entityBaseName + "}"

    }


    fun isSubclassOf(entityDef: EntityDef): Boolean {

        return this.superclassEntityDef?.let { superclassEntityDef -> superclassEntityDef == entityDef } ?: false

    }


    fun hasModifiableFields(): Boolean {

        return allEntityFieldsSorted.any { it.classFieldDef.isModifiableBySystem || it.classFieldDef.isEditableByUser.value}

    }


    fun hasNoModifiableFields(): Boolean {

        return hasModifiableFields() == false

    }


    private fun byName(fieldName: ClassFieldName): (EntityFieldDef) -> Boolean {

        return { fieldDef -> fieldDef.classFieldDef.classFieldName == fieldName }

    }


    fun findFieldByName(fieldName: String): EntityFieldDef {

        return findFieldByNameOrNull(fieldName)
            ?: throw IllegalArgumentException("No field named '$fieldName' is defined on entity '${this.entityBaseName}' with fields ${this.allEntityFieldsSorted.map { it.classFieldName }}")

    }


    fun findUniqueDatabaseIndexDefFor(classFieldName: ClassFieldName): DatabaseIndexDef? {

        return this.databaseIndexDefs
            .filter { it.isUnique }
            .filterNot { it.isMultiField }
            .find { field ->
                field.indexDef.classFieldDefs.first().classFieldName == classFieldName
            }

    }


    fun findFieldByNameOrNull(fieldName: String): EntityFieldDef? {

        return allEntityFieldsSorted
            .asSequence()
            .filter { it.classFieldDef.classFieldName.value == fieldName }
            .firstOrNull()

    }


    val hasAnyMapFields = allEntityFieldsSorted.any { it.classFieldDef.isMap }


    val hasAnyJsonFields = allEntityFieldsSorted.any { it.classFieldDef.fieldType.jdbcCompatibleType == JdbcCompatibleType.jsonb }


    fun findFieldByPath(fieldPath: FieldPath): EntityAndField {

        return getEntityFieldByPath(fieldPath, this, null)

    }


    fun findFieldByPathOrNull(fieldPath: FieldPath): EntityAndField? {

        return getEntityFieldByPathOrNull(fieldPath, this, null)

    }


    private fun getEntityFieldByPath(
        fieldPath: FieldPath,
        entityDef: EntityDef,
        referencedEntityField: EntityAndField?
    ): EntityAndField {

        val fieldName = fieldPath.head()
        val entityFieldDef = entityDef.findFieldByName(fieldName)

        return if (fieldPath.isJustOneField()) {
            EntityAndField(entityDef, entityFieldDef, referencedEntityField)
        } else {

            val fieldType = entityFieldDef.fieldType

            val foreignKeyEntityDef = if (fieldType is ForeignKeyFieldType) {
                fieldType.foreignKeyFieldDef.foreignEntityDef
            } else {
                throw RuntimeException("Entity field $fieldName on Entity ${entityDef.entityBaseName} does not reference a foreign key entity. fieldPath=$fieldPath")
            }

            getEntityFieldByPath(
                fieldPath.tail(),
                foreignKeyEntityDef,
                EntityAndField(entityDef, entityFieldDef, referencedEntityField)
            )

        }

    }


    private fun getEntityFieldByPathOrNull(
        fieldPath: FieldPath,
        entityDef: EntityDef,
        referencedEntityField: EntityAndField?
    ): EntityAndField? {

        val fieldName = fieldPath.head()
        val entityFieldDef = entityDef.findFieldByNameOrNull(fieldName)
            ?: return null

        return if (fieldPath.isJustOneField()) {
            EntityAndField(entityDef, entityFieldDef, referencedEntityField)
        } else {

            val fieldType = entityFieldDef.fieldType

            val foreignKeyEntityDef = if (fieldType is ForeignKeyFieldType) {
                fieldType.foreignKeyFieldDef.foreignEntityDef
            } else {
                throw RuntimeException("Entity field $fieldName on Entity ${entityDef.entityBaseName} does not reference a foreign key entity. fieldPath=$fieldPath")
            }

            getEntityFieldByPathOrNull(
                fieldPath.tail(),
                foreignKeyEntityDef,
                EntityAndField(entityDef, entityFieldDef, referencedEntityField)
            )

        }

    }


    fun foreignKeyFieldForBaseName(entityBaseName: EntityBaseName): EntityFieldDef {

        return this.allEntityFieldsSorted.firstOrNull { it.foreignKeyFieldDef?.foreignEntityBaseName == entityBaseName }
            ?: throw IllegalArgumentException("On entity $entityBaseName, no foreign key field references entity with base name $entityBaseName")

    }


    fun fieldPathOf(path: String): String {

        return "${entityBaseName}.$path"

    }


    val hasCreatedByIdField: Boolean
        get() = hasFieldNamed(ClassFieldName.createdById)


    val hasCreatedByUsernameField: Boolean
        get() = hasFieldNamed(ClassFieldName.createdByUsername)


    val hasCreatedByField: Boolean
        get() = hasFieldNamed(ClassFieldName.createdBy)


    val hasLastModifiedByIdField: Boolean
        get() = hasFieldNamed(ClassFieldName.lastModifiedById)


    val hasLastModifiedByUsernameField: Boolean
        get() = hasFieldNamed(ClassFieldName.lastModifiedByUsername)


    val hasVersionField: Boolean
        get() = hasFieldNamed(ClassFieldName.version)


    val hasLastModifiedTimestampUtcField: Boolean
        get() = hasFieldNamed(ClassFieldName.lastModifiedTimestampUtc)


    fun hasFieldNamed(classFieldName: ClassFieldName): Boolean {

        return allClassFieldsSorted.any { it.classFieldName == classFieldName }

    }


    fun hasNoFieldNamed(classFieldName: ClassFieldName): Boolean {

        return allClassFieldsSorted.none { it.classFieldName == classFieldName }

    }


    companion object {

        private val ABSTRACT_ENTITY_BASE_NAME = EntityBaseName("Abstract")
        private val ABSTRACT_VERSIONED_ENTITY_BASE_NAME = EntityBaseName("AbstractVersioned")
        private val ABSTRACT_ENTITY_HISTORY_BASE_NAME = EntityBaseName("AbstractHistory")
        private val PACKAGE_NAME_IGNORED = PackageName("ignored")
        private val SCHEMA_NAME_IGNORED = SchemaName("ignored")

        private val VERSION_FIELD_DEF = versionFieldDef(ABSTRACT_ENTITY_BASE_NAME, PACKAGE_NAME_IGNORED)
        private val CHANGE_TYPE_FIELD_DEF = changeTypeFieldDef(ABSTRACT_ENTITY_BASE_NAME, PACKAGE_NAME_IGNORED)

        private val ENTITY_ID_FIELD_DEF = EntityFieldDef(
            ABSTRACT_ENTITY_BASE_NAME,
            PACKAGE_NAME_IGNORED,
            aClassField(
                ClassFieldName.entityId,
                fieldType = FieldTypes.domainId
            ) {
                displayName("ID")
            }.build(),
            TableColumnName("entity_id"),
            isDeltaKey = IsDeltaKey.FALSE,
            isDeltaField = IsDeltaField.FALSE,
            isCreatableByUser = IsCreatableByUser.FALSE,
            isDerived = IsDerived.FALSE
        )


        fun abstractEntityDef(databaseType: DatabaseType): EntityDef {

            return initAbstractEntityDef(databaseType)

        }


        fun abstractVersionedEntityDef(databaseType: DatabaseType): EntityDef {

            return initAbstractVersionedEntityDef(abstractEntityDef(databaseType))

        }


        fun abstractHistoryEntityDef(databaseType: DatabaseType): EntityDef {

            return initAbstractEntityHistoryDef(
                ABSTRACT_ENTITY_HISTORY_BASE_NAME,
                abstractVersionedEntityDef(databaseType)
            )

        }

        private val GRID_FS_ENTRY_ENTITY_BASE_NAME = EntityBaseName("GridFsEntry")
        val GRID_FS_ENTITY_DEF = initGridFsEntityDef(GRID_FS_ENTRY_ENTITY_BASE_NAME)


        private fun initAbstractEntityDef(databaseType: DatabaseType): EntityDef {

            val entityBaseName = ABSTRACT_ENTITY_BASE_NAME

            val abstractEntityFields = mutableListOf<EntityFieldDef>()

            abstractEntityFields.add(
                EntityFieldDef(
                    entityBaseName,
                    PACKAGE_NAME_IGNORED,
                    aClassField(
                        ClassFieldName.id,
                        fieldType = FieldTypes.domainId,
                    ) {
                        displayName("ID")
                    }.build(),
                    TableColumnName.id(databaseType),
                    isCreatableByUser = IsCreatableByUser.FALSE
                )
            )

            abstractEntityFields.add(
                EntityFieldDef(
                    entityBaseName,
                    PACKAGE_NAME_IGNORED,
                    aClassField(
                        ClassFieldName.createdTimestampUtc,
                        fieldType = FieldTypes.instant
                    ) {
                        displayName("Created Timestamp (UTC)")
                    }.build(),
                    TableColumnName.createdTimestampUtc,
                    isCreatableByUser = IsCreatableByUser.FALSE
                )
            )

            return EntityDef(
                ABSTRACT_ENTITY_BASE_NAME,
                databaseType = DatabaseType.default(),
                tableNameOrNull = null,
                schemaName = SCHEMA_NAME_IGNORED,
                description = null,
                configurableSchemaPropertyName = null,
                isCappedCollection = IsCappedCollection.FALSE,
                cappedSizeInBytes = null,
                packageName = PackageName("org.maiaframework.domain"),
                isDeltaEntity = IsDeltaEntity.FALSE,
                entityFieldsNotInherited = abstractEntityFields,
                superclassEntityDef = null,
                isAbstract = true,
                typeDiscriminatorOrNull = null,
                withHandCodedDao = WithHandCodedDao.FALSE,
                withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
                deletable = Deletable.FALSE,
                allowDeleteAll = AllowDeleteAll.FALSE,
                allowFindAll = AllowFindAll.FALSE,
                providedIndexDefs = emptyList(),
                withVersionHistory = WithVersionHistory.FALSE,
                versioned = Versioned.FALSE,
                isHistoryEntity = false,
                moduleName = null,
                daoHasSpringAnnotation = DaoHasSpringAnnotation.FALSE,
                entityDaoHasSpringAnnotation = EntityDaoHasSpringAnnotation.FALSE,
                hasEffectiveTimestamps = HasEffectiveTimestamps.FALSE,
                hasEffectiveLocalDates = HasEffectiveLocalDates.FALSE,
                hasEntityDetailDtoDef = HasEntityDetailDtoDef.FALSE,
                hasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
                cacheableDef = null,
                crudDef = CrudDef.EMPTY
            )

        }


        private fun initAbstractEntityHistoryDef(
            entityBaseName: EntityBaseName,
            superclassEntityDef: EntityDef
        ): EntityDef {

            val abstractEntityFields = mutableListOf<EntityFieldDef>()

            abstractEntityFields.add(ENTITY_ID_FIELD_DEF)
            abstractEntityFields.add(CHANGE_TYPE_FIELD_DEF)

            return EntityDef(
                entityBaseName,
                databaseType = superclassEntityDef.databaseType,
                tableNameOrNull = null,
                schemaName = SCHEMA_NAME_IGNORED,
                description = null,
                configurableSchemaPropertyName = null,
                isCappedCollection = IsCappedCollection.FALSE,
                cappedSizeInBytes = null,
                packageName = PackageName("org.maiaframework.domain"),
                isDeltaEntity = IsDeltaEntity.FALSE,
                entityFieldsNotInherited = abstractEntityFields,
                superclassEntityDef = superclassEntityDef,
                isAbstract = true,
                typeDiscriminatorOrNull = null,
                withHandCodedDao = WithHandCodedDao.FALSE,
                withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
                deletable = Deletable.FALSE,
                allowDeleteAll = AllowDeleteAll.FALSE,
                allowFindAll = AllowFindAll.FALSE,
                providedIndexDefs = emptyList(),
                withVersionHistory = WithVersionHistory.FALSE,
                versioned = Versioned.TRUE,
                isHistoryEntity = false,
                moduleName = null,
                daoHasSpringAnnotation = DaoHasSpringAnnotation.FALSE,
                entityDaoHasSpringAnnotation = EntityDaoHasSpringAnnotation.FALSE,
                hasEffectiveTimestamps = HasEffectiveTimestamps.FALSE,
                hasEffectiveLocalDates = HasEffectiveLocalDates.FALSE,
                hasEntityDetailDtoDef = HasEntityDetailDtoDef.FALSE,
                hasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
                cacheableDef = null,
                crudDef = CrudDef.EMPTY,
            )

        }


        private fun initAbstractVersionedEntityDef(
            superclassEntityDef: EntityDef
        ): EntityDef {

            val abstractEntityFields = listOf(VERSION_FIELD_DEF)

            return EntityDef(
                allowDeleteAll = AllowDeleteAll.FALSE,
                allowFindAll = AllowFindAll.FALSE,
                cacheableDef = null,
                cappedSizeInBytes = null,
                tableNameOrNull = null,
                configurableSchemaPropertyName = null,
                crudDef = CrudDef.EMPTY,
                daoHasSpringAnnotation = DaoHasSpringAnnotation.FALSE,
                databaseType = DatabaseType.default(),
                deletable = Deletable.FALSE,
                description = null,
                entityBaseName = ABSTRACT_VERSIONED_ENTITY_BASE_NAME,
                entityDaoHasSpringAnnotation = EntityDaoHasSpringAnnotation.FALSE,
                entityFieldsNotInherited = abstractEntityFields,
                hasEffectiveLocalDates = HasEffectiveLocalDates.FALSE,
                hasEffectiveTimestamps = HasEffectiveTimestamps.FALSE,
                hasEntityDetailDtoDef = HasEntityDetailDtoDef.FALSE,
                hasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
                isAbstract = true,
                isCappedCollection = IsCappedCollection.FALSE,
                isDeltaEntity = IsDeltaEntity.FALSE,
                isHistoryEntity = false,
                moduleName = null,
                packageName = PackageName("org.maiaframework.domain"),
                providedIndexDefs = emptyList(),
                schemaName = SCHEMA_NAME_IGNORED,
                superclassEntityDef = superclassEntityDef,
                typeDiscriminatorOrNull = null,
                versioned = Versioned.TRUE,
                withHandCodedDao = WithHandCodedDao.FALSE,
                withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
                withVersionHistory = WithVersionHistory.FALSE
            )

        }


        fun idFieldDef(
            entityBaseName: EntityBaseName,
            packageName: PackageName
        ): EntityFieldDef {

            return EntityFieldDef(
                entityBaseName,
                packageName,
                aClassField(
                    ClassFieldName.id,
                    fieldType = FieldTypes.domainId
                ) {
                    displayName("ID")
                }.build(),
                TableColumnName.id,
                isDeltaKey = IsDeltaKey.FALSE,
                isDeltaField = IsDeltaField.FALSE,
                isPrimaryKey = IsPrimaryKey.TRUE,
                isDerived = IsDerived.FALSE,
                isCreatableByUser = IsCreatableByUser.FALSE
            )

        }


        fun createdTimestampUtcFieldDef(
            entityBaseName: EntityBaseName,
            packageName: PackageName
        ): EntityFieldDef {

            return EntityFieldDef(
                entityBaseName,
                packageName,
                aClassField(
                    ClassFieldName.createdTimestampUtc,
                    fieldType = FieldTypes.instant
                ) {
                    displayName("Created At")
                }.build(),
                TableColumnName.createdTimestampUtc,
                isCreatableByUser = IsCreatableByUser.FALSE
            )

        }


        fun versionFieldDef(
            entityBaseName: EntityBaseName,
            packageName: PackageName
        ): EntityFieldDef {

            return EntityFieldDef(
                entityBaseName,
                packageName,
                aClassField(
                    ClassFieldName.version,
                    fieldType = FieldTypes.long
                ) {
                    displayName("Version")
                }.build(),
                TableColumnName.version,
                isCreatableByUser = IsCreatableByUser.FALSE
            )

        }


        fun changeTypeFieldDef(entityBaseName: EntityBaseName, packageName: PackageName): EntityFieldDef {

            return EntityFieldDef(
                entityBaseName,
                packageName,
                aClassField(
                    ClassFieldName.changeType,
                    fieldType = FieldTypes.enum(EnumDefs.CHANGE_TYPE_ENUM_DEF),
                ) {
                    displayName("Change Type")
                    lengthConstraint(max = 10)
                }.build(),
                TableColumnName.changeType,
                isCreatableByUser = IsCreatableByUser.FALSE
            )

        }


        private fun initGridFsEntityDef(entityBaseName: EntityBaseName): EntityDef {

            val gridFsEntityFields = mutableListOf<EntityFieldDef>()

            gridFsEntityFields.add(
                EntityFieldDef(
                    entityBaseName,
                    PACKAGE_NAME_IGNORED,
                    aClassField(
                        classFieldName = "gridFsId",
                        fieldType = FieldTypes.objectId
                    ) {
                        displayName("GridFS ID")
                        unique()
                    }.build(),
                    TableColumnName("gfs-id"),
                    isDeltaKey = IsDeltaKey.FALSE,
                    isDeltaField = IsDeltaField.FALSE,
                    isDerived = IsDerived.FALSE,
                    isCreatableByUser = IsCreatableByUser.FALSE
                )
            )

            gridFsEntityFields.add(
                EntityFieldDef(
                    entityBaseName,
                    PACKAGE_NAME_IGNORED,
                    aClassField(
                        ClassFieldName("deleted"),
                        fieldType = FieldTypes.boolean
                    ) {
                        displayName("Deleted")
                        modifiable()
                    }.build(),
                    TableColumnName("del"),
                    isDeltaKey = IsDeltaKey.FALSE,
                    isDeltaField = IsDeltaField.FALSE,
                    isDerived = IsDerived.FALSE,
                    isCreatableByUser = IsCreatableByUser.FALSE
                )
            )

            return EntityDef(
                GRID_FS_ENTRY_ENTITY_BASE_NAME,
                databaseType = DatabaseType.default(),
                tableNameOrNull = null,
                schemaName = SCHEMA_NAME_IGNORED,
                configurableSchemaPropertyName = null,
                description = null,
                isCappedCollection = IsCappedCollection.FALSE,
                cappedSizeInBytes = null,
                packageName = PackageName("org.maiaframework.domain.gridfs"),
                isDeltaEntity = IsDeltaEntity.FALSE,
                entityFieldsNotInherited = gridFsEntityFields.toList(),
                superclassEntityDef = abstractEntityDef(DatabaseType.MONGO),
                isAbstract = true,
                typeDiscriminatorOrNull = null,
                withHandCodedDao = WithHandCodedDao.FALSE,
                withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
                deletable = Deletable.FALSE,
                allowDeleteAll = AllowDeleteAll.FALSE,
                allowFindAll = AllowFindAll.FALSE,
                providedIndexDefs = listOf(),
                withVersionHistory = WithVersionHistory.FALSE,
                versioned = Versioned.FALSE,
                isHistoryEntity = false,
                moduleName = null,
                daoHasSpringAnnotation = DaoHasSpringAnnotation.FALSE,
                entityDaoHasSpringAnnotation = EntityDaoHasSpringAnnotation.FALSE,
                hasEffectiveTimestamps = HasEffectiveTimestamps.FALSE,
                hasEffectiveLocalDates = HasEffectiveLocalDates.FALSE,
                hasEntityDetailDtoDef = HasEntityDetailDtoDef.FALSE,
                hasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
                cacheableDef = null,
                crudDef = CrudDef.EMPTY
            )

        }


    }


}
