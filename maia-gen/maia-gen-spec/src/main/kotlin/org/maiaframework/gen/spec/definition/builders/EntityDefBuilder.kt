package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.domain.types.TypeDiscriminator
import org.maiaframework.gen.spec.definition.AngularFormType
import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.ConfigurableSchemaPropertyName
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EnumDefs
import org.maiaframework.gen.spec.definition.ForeignKeyFieldDef
import org.maiaframework.gen.spec.definition.IndexDef
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.LongTypeDef
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
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
import org.maiaframework.gen.spec.definition.flags.IsDeltaEntity
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.Versioned
import org.maiaframework.gen.spec.definition.flags.WithCrudListener
import org.maiaframework.gen.spec.definition.flags.WithHandCodedDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEntityDao
import org.maiaframework.gen.spec.definition.flags.WithVersionHistory
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.jdbc.TableName

@MaiaDslMarker
class EntityDefBuilder(
    private val packageName: PackageName,
    val entityBaseName: EntityBaseName,
    private val isDeltaEntity: IsDeltaEntity,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?,
    private val withVersionHistory: WithVersionHistory,
    private val versioned: Versioned,
    private val deletable: Deletable = Deletable.FALSE,
    private val allowDeleteAll: AllowDeleteAll,
    private val allowFindAll: AllowFindAll,
    private val nameFieldForInAndNameDto: String?,
    private val withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
    private val withHandcodedEntityDao: WithHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
    defaultSchemaName: SchemaName,
    private val databaseType: DatabaseType
) {


    private val entityFieldDefBuilders = mutableListOf<EntityFieldDefBuilder>()


    private val foreignKeyFieldDefBuilders = mutableListOf<ForeignKeyFieldDefBuilder>()


    private val indexDefBuilders = mutableListOf<IndexDefBuilder>()


    private var tableName: TableName? = null


    private var moduleName: ModuleName? = null


    private var angularFormType: AngularFormType = AngularFormType.REACTIVE


    private var description: Description? = null


    private var isCappedCollection: IsCappedCollection = IsCappedCollection.FALSE


    private var cappedSizeInBytes: Long? = null


    var isAbstract: Boolean = false


    private var superclassEntityDef: EntityDef? = null


    private var typeDiscriminator: TypeDiscriminator? = null


    private val crudDefBuilder = CrudDefBuilder()


    private var hasEntityDetailDtoDef: HasEntityDetailDtoDef = HasEntityDetailDtoDef.FALSE


    private var cacheableDefBuilder: EntityCacheableDefBuilder? = null


    private var configurableSchemaPropertyName: ConfigurableSchemaPropertyName? = null


    private var schemaName: SchemaName = defaultSchemaName


    var daoHasSpringAnnotation: Boolean = true


    var entityDaoHasSpringAnnotation: Boolean = true


    private var hasEffectiveLocalDates: Boolean = false


    private var hasEffectiveTimestamps: Boolean = false


    private var hasSingleEffectiveRecord: Boolean = true


    fun build(): EntityDef {

        val entityFieldDefs = buildEntityFieldDefs()
        val indexDefs = buildIndexDefs(entityFieldDefs)
        val crudDef = this.crudDefBuilder.build(this.superclassEntityDef)
        val cacheableDef = this.cacheableDefBuilder?.build()

        val recordVersionHistory = if (this.superclassEntityDef?.withVersionHistory?.value == true) {
            WithVersionHistory.TRUE
        } else {
            this.withVersionHistory
        }

        return EntityDef(
            this.entityBaseName,
            this.tableName,
            this.schemaName,
            this.databaseType,
            this.configurableSchemaPropertyName,
            this.isCappedCollection,
            this.cappedSizeInBytes,
            this.packageName,
            this.description,
            this.moduleName,
            this.isDeltaEntity,
            entityFieldDefs,
            this.superclassEntityDef,
            this.isAbstract,
            this.typeDiscriminator,
            this.withHandcodedDao,
            this.withHandcodedEntityDao,
            this.deletable,
            DaoHasSpringAnnotation(this.daoHasSpringAnnotation),
            EntityDaoHasSpringAnnotation(this.entityDaoHasSpringAnnotation),
            this.allowDeleteAll,
            this.allowFindAll,
            indexDefs,
            crudDef,
            recordVersionHistory,
            this.versioned,
            isHistoryEntity = false,
            nameFieldForInAndNameDto,
            stagingEntityFieldDefs = emptyList(),
            HasEffectiveTimestamps(this.hasEffectiveTimestamps),
            HasEffectiveLocalDates(this.hasEffectiveLocalDates),
            HasSingleEffectiveRecord(this.hasSingleEffectiveRecord),
            hasEntityDetailDtoDef,
            cacheableDef,
            this.angularFormType
        )

    }


    private fun buildEntityFieldDefs(): List<EntityFieldDef> {

        val entityFieldDefs = this.entityFieldDefBuilders.map { it.build() }

        val noPrimaryKeyFields = entityFieldDefs.none { it.isPrimaryKey.value }

        val idField = if (noPrimaryKeyFields && this.superclassEntityDef == null) {

            EntityDef.idFieldDef(entityBaseName, packageName)

        } else {

            null

        }

        val createdTimestampUtcField = if (this.superclassEntityDef == null) {

            EntityDef.createdTimestampUtcFieldDef(entityBaseName, packageName)

        } else {

            null

        }

        val versionField = if (this.versioned.value && this.superclassEntityDef == null) {

            EntityDef.versionFieldDef(entityBaseName, packageName)

        } else {

            null

        }

        val foreignKeyFieldDefs = this.foreignKeyFieldDefBuilders.map { it.buildEntityFieldDef(this.entityBaseName, this.packageName) }

        return entityFieldDefs
            .plus(idField)
            .plus(foreignKeyFieldDefs)
            .plus(createdTimestampUtcField)
            .plus(versionField)
            .filterNotNull()

    }


    private fun buildIndexDefs(entityFieldDefs: List<EntityFieldDef>): List<IndexDef> {

        val allFields = mutableListOf<EntityFieldDef>()
        allFields.addAll(entityFieldDefs)
        this.superclassEntityDef?.allEntityFields?.let { allFields.addAll(it) }

        val uniqueFieldIndexDefBuilders = allFields
            .asSequence()
            .filter { it.classFieldDef.isUnique }
            .map { fd ->

                val indexDefBuilder = IndexDefBuilder(this.entityBaseName)
                    .withFieldAscending(fd.classFieldDef.classFieldName.value)
                    .unique()

                if (fd.classFieldDef.nullable || this.typeDiscriminator != null) {
                    indexDefBuilder.sparse()
                }

                if (fd.withExistsEndpoint) {
                    indexDefBuilder.withExistsEndpoint()
                }

                indexDefBuilder

            }.toList()

        return listOf(uniqueFieldIndexDefBuilders, this.indexDefBuilders)
            .flatten()
            .map { indexDefBuilder -> indexDefBuilder.build(allFields) }

    }


    fun superclass(superclassEntityDef: EntityDef) {

        this.superclassEntityDef = superclassEntityDef

    }


    fun tableName(
        name: String,
        viewName: String? = null,
        capped: Boolean = false,
        cappedSizeInBytes: Long? = null
    ) {

        this.tableName = TableName(name, viewName)
        this.isCappedCollection = IsCappedCollection(capped)
        this.cappedSizeInBytes = cappedSizeInBytes

    }


    fun description(description: String) {

        this.description = Description(description)

    }


    fun moduleName(moduleName: String) {

        this.moduleName = ModuleName.of(moduleName)

    }


    fun withEffectiveTimestamps(hasSingleEffectiveRecord: Boolean = true) {

        field("effectiveFrom", FieldTypes.instant) {
            nullable()
            modifiableBySystem()
        }

        field("effectiveTo", FieldTypes.instant) {
            nullable()
            modifiableBySystem()
        }

        this.hasEffectiveTimestamps = true
        this.hasSingleEffectiveRecord = hasSingleEffectiveRecord

        validateEffectiveTimeFields()

    }


    fun withEffectiveLocalDates(
        effectiveFromDescription: String? = null,
        effectiveToDescription: String? = null,
        hasSingleEffectiveRecord: Boolean = true
    ) {

        field("effectiveFrom", FieldTypes.localDate) {
            effectiveFromDescription?.let { description(it) }
            nullable()
            editableByUser()
        }

        field("effectiveTo", FieldTypes.localDate) {
            effectiveToDescription?.let { description(it) }
            nullable()
            editableByUser()
        }

        this.hasEffectiveLocalDates = true
        this.hasSingleEffectiveRecord = hasSingleEffectiveRecord

        validateEffectiveTimeFields()

    }


    private fun validateEffectiveTimeFields() {

        require((this.hasEffectiveTimestamps && this.hasEffectiveLocalDates) == false) { "cannot set both effectiveLocalDates and effectiveTimestamps" }

    }


    fun typeDiscriminator(typeDiscriminator: String) {

        this.typeDiscriminator = TypeDiscriminator(typeDiscriminator)

    }


    fun angularFormType(angularFormType: AngularFormType) {

        this.angularFormType = angularFormType

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), fieldType)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        dtoDef: SimpleResponseDtoDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), FieldTypes.responseDto(dtoDef))
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        foreignKeyFieldDef: ForeignKeyFieldDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), foreignKeyFieldDef)
        init?.invoke(builder)

    }


    fun foreignKey(
        fieldName: String,
        entityDef: EntityDef,
        isEditableByUser: IsEditableByUser = IsEditableByUser.FALSE,
        fieldDisplayName: String? = null,
        init: (ForeignKeyFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = ForeignKeyFieldDefBuilder(ClassFieldName(fieldName), entityDef)
        fieldDisplayName?.let { builder.fieldDisplayName(it) }
        this.foreignKeyFieldDefBuilders.add(builder)

        if (isEditableByUser.value) {
            builder.editableByUser()
        }

        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        enumDef: EnumDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), FieldTypes.enum(enumDef))
        init?.invoke(builder)

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        fieldType: FieldType
    ): EntityFieldDefBuilder {

        return add(
            EntityFieldDefBuilder(
                classFieldName,
                fieldType,
                this.entityBaseName,
                this.packageName,
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.stringType(stringTypeDef)
        )

        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        intTypeDef: IntTypeDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.intType(intTypeDef)
        )

        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        longTypeDef: LongTypeDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.longType(longTypeDef)
        )

        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        booleanTypeDef: BooleanTypeDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.booleanType(booleanTypeDef)
        )

        init?.invoke(builder)

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        foreignKeyFieldDef: ForeignKeyFieldDef
    ): EntityFieldDefBuilder {

        return add(
            EntityFieldDefBuilder(
                classFieldName,
                foreignKeyFieldDef,
                this.entityBaseName,
                this.packageName,
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )


    }


    private fun add(builder: EntityFieldDefBuilder): EntityFieldDefBuilder {

        this.entityFieldDefBuilders.add(builder)
        return builder

    }


    fun field(
        fieldName: String,
        valueClassDef: StringValueClassDef,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.stringValueClass(valueClassDef)
        )

        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        listFieldType: ListFieldType,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), listFieldType)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        mapFieldType: MapFieldType,
        init: (EntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), mapFieldType)
        init?.invoke(builder)

    }


    fun field_createdById(entityDef: EntityDef, nullable: Boolean = false) {

        foreignKey(ClassFieldName.createdBy.value, entityDef, fieldDisplayName = "Created By") {
            tableColumnName(TableColumnName.createdById.value)
            notCreatableByUser()
            if (nullable) nullable()
        }

    }


    fun field_createdByName(nullable: Boolean = false, maxLength: Long = 100) {

        field(ClassFieldName.createdByUsername.value, FieldTypes.string) {
            fieldDisplayName("Created By")
            tableColumnName(TableColumnName.createdByName.value)
            if (nullable) nullable()
            lengthConstraint(max = maxLength)
        }

    }


    fun field_lastModifiedById(entityDef: EntityDef, nullable: Boolean = false) {

        foreignKey(ClassFieldName.lastModifiedBy, entityDef, fieldDisplayName = "Last Modified By") {
            tableColumnName(TableColumnName.lastModifiedById.value)
            notCreatableByUser()
            modifiableBySystem()
            if (nullable) nullable()
        }

    }


    fun field_lastModifiedByName(nullable: Boolean = false, maxLength: Long = 100) {

        field(ClassFieldName.lastModifiedByUsername.value, FieldTypes.string) {
            fieldDisplayName("Last Modified By")
            tableColumnName(TableColumnName.lastModifiedByName.value)
            notCreatableByUser()
            modifiableBySystem()
            if (nullable) nullable()
            lengthConstraint(max = maxLength)
        }

    }


    fun field_lastModifiedTimestampUtc() {

        field(ClassFieldName.lastModifiedTimestampUtc.value, FieldTypes.instant) {
            fieldDisplayName("Last Modified Timestamp (UTC)")
            tableColumnName(TableColumnName.lastModifiedTimestampUtc.value)
            notCreatableByUser()
            modifiableBySystem()
        }

    }


    fun field_lifecycleState() {

        field(ClassFieldName.lifecycleState.value, FieldTypes.enum(EnumDefs.LIFECYCLE_STATE_ENUM_DEF)) {
            fieldDisplayName("Lifecycle State")
            tableColumnName(TableColumnName.lifecycleState.value)
            notCreatableByUser()
            modifiableBySystem()
        }

    }


    fun index(
        init: IndexDefBuilder.() -> Unit
    ): IndexDefBuilder {

        val indexDefBuilder = IndexDefBuilder(this.entityBaseName)
        indexDefBuilder.init()
        this.indexDefBuilders.add(indexDefBuilder)
        return indexDefBuilder

    }


    fun crud(
        init: CrudDefBuilder.() -> Unit
    ) {

        this.crudDefBuilder.withCrudListener = WithCrudListener.TRUE
        this.crudDefBuilder.init()

    }


    fun cacheable(init: EntityCacheableDefBuilder.() -> Unit) {

        val builder = EntityCacheableDefBuilder(this.entityBaseName)
        this.cacheableDefBuilder = builder
        builder.init()

    }


    fun withDetailDto() {

        this.hasEntityDetailDtoDef = HasEntityDetailDtoDef.TRUE

    }


}
