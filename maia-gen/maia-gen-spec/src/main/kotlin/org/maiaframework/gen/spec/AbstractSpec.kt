package org.maiaframework.gen.spec

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.definition.AngularComponentBaseName
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.BlotterSearchableDtoSourceDef
import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.BooleanValueClassDef
import org.maiaframework.gen.spec.definition.DataClassDef
import org.maiaframework.gen.spec.definition.DataClassName
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.DisplayName
import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.ElasticIndexBaseName
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.EntityHtmlFormDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EnumDefs
import org.maiaframework.gen.spec.definition.EnumValueDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.FormModelClassName
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.HazelcastDtoDef
import org.maiaframework.gen.spec.definition.HtmlFormDef
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.LongTypeDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ModelDefinitionException
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.ResponseDtoDef
import org.maiaframework.gen.spec.definition.RowMapperDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.TypeaheadName
import org.maiaframework.gen.spec.definition.builders.AngularFormDefBuilder
import org.maiaframework.gen.spec.definition.builders.BlotterDefBuilder
import org.maiaframework.gen.spec.definition.builders.ManyToManyEntityDefBuilder
import org.maiaframework.gen.spec.definition.builders.BlotterPageDefBuilder
import org.maiaframework.gen.spec.definition.builders.BooleanTypeDefBuilder
import org.maiaframework.gen.spec.definition.builders.BooleanValueClassDefBuilder
import org.maiaframework.gen.spec.definition.builders.DataClassDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityCreateHtmlFormDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityCreatePageDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityDetailViewDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityEditPageDefBuilder
import org.maiaframework.gen.spec.definition.builders.EnumDefBuilder
import org.maiaframework.gen.spec.definition.builders.EsDocDefBuilder
import org.maiaframework.gen.spec.definition.builders.FixedWidthFileStagingEntityDefBuilder
import org.maiaframework.gen.spec.definition.builders.FormModelDefBuilder
import org.maiaframework.gen.spec.definition.builders.HazelcastDtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.IntTypeDefBuilder
import org.maiaframework.gen.spec.definition.builders.LongTypeDefBuilder
import org.maiaframework.gen.spec.definition.builders.MaiaDslMarker
import org.maiaframework.gen.spec.definition.builders.RequestDtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.RequestDtoHtmlFormDefBuilder
import org.maiaframework.gen.spec.definition.builders.ResponseDtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.SearchableDtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.SimpleResponseDtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.StringTypeDefBuilder
import org.maiaframework.gen.spec.definition.builders.StringValueClassDefBuilder
import org.maiaframework.gen.spec.definition.builders.TypeaheadDefBuilder
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsDeltaEntity
import org.maiaframework.gen.spec.definition.flags.Versioned
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.flags.WithHandCodedDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEntityDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEsDocRepo
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithVersionHistory
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.lang.text.StringFunctions


abstract class AbstractSpec protected constructor(
    appKey: AppKey,
    defaultSchemaName: SchemaName? = null
) : ModelDefProvider {


    private val angularFormDefs = mutableListOf<AngularFormDef>()
    private val authorityDefs = mutableListOf<AuthorityDef>()
    private val booleanTypeDefs = mutableListOf<BooleanTypeDef>()
    private val booleanValueClassDefs = mutableListOf<BooleanValueClassDef>()
    private val blotterPageDefs = mutableListOf<BlotterPageDef>()
    private val dataClassDefs = mutableListOf<DataClassDef>()
    private val defaultSchemaName = defaultSchemaName ?: SchemaName(appKey.value)
    private val blotterDefs = mutableListOf<BlotterDef>()
    private val entityCreateHtmlFormDefs = mutableListOf<EntityHtmlFormDef>()
    private val entityDetailViewDefs = mutableListOf<EntityDetailViewDef>()
    private val entityCreatePageDefs = mutableListOf<EntityCreatePageDef>()
    private val entityEditPageDefs = mutableListOf<EntityEditPageDef>()
    private val entityDefs = mutableListOf<EntityDef>()
    private val enumDefs = mutableListOf<EnumDef>().also { it.add(EnumDefs.LIFECYCLE_STATE_ENUM_DEF) }
    private val esDocDefs = mutableListOf<EsDocDef>()
    private val fieldReadersByFieldType = mutableMapOf<FieldType, ParameterizedType>()
    private val fieldWritersByFieldType = mutableMapOf<FieldType, ParameterizedType>()
    private val formModelDefs = mutableListOf<FormModelDef>()
    private val hazelcastDtoDefs = mutableListOf<HazelcastDtoDef>()
    private val intTypeDefs = mutableListOf<IntTypeDef>()
    private val longTypeDefs = mutableListOf<LongTypeDef>()
    private val requestDtoDefs = mutableListOf<RequestDtoDef>()
    private val requestDtoHtmlFormDefs = mutableListOf<HtmlFormDef>()
    private val responseDtoDefs = mutableListOf<ResponseDtoDef>()
    private val rootEntityHierarchies = mutableListOf<EntityHierarchy>()
    private val rowMapperDefs = mutableListOf<RowMapperDef>()
    private val searchableDtoDefs = mutableListOf<SearchableDtoDef>()
    private val simpleResponseDtoDefs = mutableListOf<SimpleResponseDtoDef>()
    private val stringTypeDefs = mutableListOf<StringTypeDef>()
    private val stringValueClassDefs = mutableListOf<StringValueClassDef>()
    private val typeaheadDefs = mutableListOf<TypeaheadDef>()
    private val manyToManyAssociationsByEntityName: MutableMap<EntityBaseName, MutableList<ManyToManyEntityDef>> = mutableMapOf()

    private val lookupFieldReaderByFieldType = { fieldType: FieldType -> this.fieldReadersByFieldType[fieldType] }
    private val lookupFieldWriterByFieldType = { fieldType: FieldType -> this.fieldWritersByFieldType[fieldType] }


    override val modelDef: ModelDef by lazy {

            populateEntityHierarchy()
            finalizeEntityDefs()

            ModelDef(
                this.rootEntityHierarchies,
                this.authorityDefs,
                this.formModelDefs,
                this.entityCreateHtmlFormDefs,
                this.requestDtoDefs,
                this.responseDtoDefs,
                this.simpleResponseDtoDefs,
                this.hazelcastDtoDefs,
                this.searchableDtoDefs,
                this.blotterDefs,
                this.requestDtoHtmlFormDefs,
                this.angularFormDefs,
                this.dataClassDefs,
                this.enumDefs,
                this.booleanTypeDefs,
                this.intTypeDefs,
                this.longTypeDefs,
                this.stringTypeDefs,
                this.typeaheadDefs,
                this.esDocDefs,
                this.rowMapperDefs,
                this.entityDetailViewDefs,
                this.entityEditPageDefs,
                this.entityCreatePageDefs,
                this.blotterPageDefs,
            )

        }


    private fun finalizeEntityDefs() {

        entityDefs.forEach { entityDef ->
            entityDef.initManyToManyAssociations(
                manyToManyAssociationsByEntityName[entityDef.entityBaseName] ?: emptyList()
            )
        }

        val violations = entityDefs
            .filter { it.withVersionHistory.value }
            .flatMap { entityDef ->
                entityDef.allForeignKeyEntityFieldDefs
                    .map { it.foreignKeyFieldDef!!.foreignEntityDef }
                    .filter { !it.withVersionHistory.value && it.deletable.value }
                    .map { foreignEntityDef -> Pair(entityDef, foreignEntityDef) }
            }

        if (violations.isNotEmpty()) {
            val details = violations.joinToString("\n") { (entityDef, foreignEntityDef) ->
                "  - '${entityDef.entityBaseName}' references '${foreignEntityDef.entityBaseName}' which does not have version history"
            }
            throw ModelDefinitionException(
                "Entities with history tables may only reference entities that also have history tables or are non-deletable:\n$details"
            )
        }

    }


    private fun populateEntityHierarchy() {

        val allEntityBaseNames = mutableSetOf<EntityBaseName>()

        for (entityDef in this.entityDefs) {

            val entityBaseNameExists = allEntityBaseNames.add(entityDef.entityBaseName) == false

            if (entityBaseNameExists) {
                throw RuntimeException("Duplicate entity base name: " + entityDef.entityBaseName)
            }

            addToEntityHierarchy(entityDef)

            entityDef.historyEntityDef?.let { addToEntityHierarchy(it) }

        }

    }


    private fun addToEntityHierarchy(entityDef: EntityDef) {

        if (entityDef.isRootEntity) {

            val entityHierarchy = EntityHierarchy(entityDef)
            this.rootEntityHierarchies.add(entityHierarchy)

        } else {

            findEntityHierarchyFor(entityDef)

        }


    }


    private fun findEntityHierarchyFor(entityDef: EntityDef) {

        for (entityHierarchy in this.rootEntityHierarchies) {

            if (entityHierarchy.addToHierarchyIfItBelongs(entityDef)) {
                return
            }

        }

        throw RuntimeException("Could not find an existing entity hierarchy for entity: " + entityDef.entityBaseName)

    }


    protected fun defaultFieldReader(fieldType: FieldType, fieldReaderClass: Class<*>) {

        this.fieldReadersByFieldType[fieldType] = ParameterizedType(Fqcn.valueOf(fieldReaderClass))

    }


    protected fun defaultFieldReader(fieldType: FieldType, fieldReaderFqcn: Fqcn) {

        this.fieldReadersByFieldType[fieldType] = ParameterizedType(fieldReaderFqcn)

    }


    protected fun defaultFieldWriter(fieldType: FieldType, fieldWriterClass: Class<*>) {

        this.fieldWritersByFieldType[fieldType] = ParameterizedType(Fqcn.valueOf(fieldWriterClass))

    }


    protected fun defaultFieldWriter(fieldType: FieldType, fieldWriterFqcn: Fqcn) {

        this.fieldWritersByFieldType[fieldType] = ParameterizedType(fieldWriterFqcn)

    }


    protected fun enumDef(
        packageName: String,
        enumName: String,
        init: (EnumDefBuilder.() -> Unit)? = null
    ): EnumDef {

        val builder = EnumDefBuilder(PackageName(packageName).uqcn(enumName))
        init?.invoke(builder)
        val def = builder.build()
        this.enumDefs.add(def)
        return def

    }


    protected fun enumDef(
        rawFqcn: String,
        init: EnumDefBuilder.() -> Unit
    ): EnumDef {

        val builder = EnumDefBuilder(Fqcn.valueOf(rawFqcn))
        builder.init()
        val def = builder.build()
        this.enumDefs.add(def)
        return def

    }


    protected fun stringType(
        rawFqcn: String,
        init: (StringTypeDefBuilder.() -> Unit)? = null
    ): StringTypeDef {

        val builder = StringTypeDefBuilder(rawFqcn)
        init?.invoke(builder)
        val stringTypeDef = builder.build()
        this.stringTypeDefs.add(stringTypeDef)
        return stringTypeDef

    }


    protected fun stringType(
        packageName: String,
        typeName: String,
        init: (StringTypeDefBuilder.() -> Unit)? = null
    ): StringTypeDef {

        val builder = StringTypeDefBuilder(packageName, typeName)
        init?.invoke(builder)
        val stringTypeDef = builder.build()
        this.stringTypeDefs.add(stringTypeDef)
        return stringTypeDef

    }


    protected fun stringValueClass(
        packageName: String,
        typeName: String,
        init: (StringValueClassDefBuilder.() -> Unit)? = null
    ): StringValueClassDef {

        val builder = StringValueClassDefBuilder(packageName, typeName)
        init?.invoke(builder)
        val def = builder.build()
        this.stringValueClassDefs.add(def)
        return def

    }


    protected fun booleanValueClass(
        packageName: String,
        typeName: String,
        init: (BooleanValueClassDefBuilder.() -> Unit)? = null
    ): BooleanValueClassDef {

        val builder = BooleanValueClassDefBuilder(packageName, typeName)
        init?.invoke(builder)
        val def = builder.build()
        this.booleanValueClassDefs.add(def)
        return def

    }


    protected fun intType(
        rawFqcn: String,
        init: (IntTypeDefBuilder.() -> Unit)? = null
    ): IntTypeDef {

        val builder = IntTypeDefBuilder(rawFqcn)
        init?.invoke(builder)
        val def = builder.build()
        this.intTypeDefs.add(def)
        return def

    }


    protected fun intType(
        packageName: String,
        typeName: String,
        init: (IntTypeDefBuilder.() -> Unit)? = null
    ): IntTypeDef {

        val builder = IntTypeDefBuilder(packageName, typeName)
        init?.invoke(builder)
        val def = builder.build()
        this.intTypeDefs.add(def)
        return def

    }


    protected fun longType(
        rawFqcn: String,
        init: (LongTypeDefBuilder.() -> Unit)? = null
    ): LongTypeDef {

        val builder = LongTypeDefBuilder(rawFqcn)
        init?.invoke(builder)
        val def = builder.build()
        this.longTypeDefs.add(def)
        return def

    }


    protected fun longType(
        subpackage: String,
        typeName: String,
        init: (LongTypeDefBuilder.() -> Unit)? = null
    ): LongTypeDef {

        val builder = LongTypeDefBuilder(subpackage, typeName)
        init?.invoke(builder)
        val def = builder.build()
        this.longTypeDefs.add(def)
        return def

    }


    protected fun booleanType(
        rawFqcn: String,
        init: (BooleanTypeDefBuilder.() -> Unit)? = null
    ): BooleanTypeDef {

        val builder = BooleanTypeDefBuilder(rawFqcn)
        init?.invoke(builder)
        val def = builder.build()
        this.booleanTypeDefs.add(def)
        return def

    }


    protected fun booleanType(
        packageName: String,
        typeName: String,
        init: (BooleanTypeDefBuilder.() -> Unit)? = null
    ): BooleanTypeDef {

        val builder = BooleanTypeDefBuilder(packageName, typeName)
        init?.invoke(builder)
        val def = builder.build()
        this.booleanTypeDefs.add(def)
        return def

    }


    /**
     * A Simple many-to-many join is not versioned and has no history. The join either
     * exists or it doesn't. An optional effective range may be configured via the builder.
     */
    protected fun simpleManyToManyEntity(
        packageName: String,
        entityBaseName: String,
        withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
        leftEntity: ReferencedEntity,
        rightEntity: ReferencedEntity,
        pkAndNameFieldName: String? = null,
        init: (ManyToManyEntityDefBuilder.() -> Unit)? = null
    ): ManyToManyEntityDef {

        val manyToManyBuilder = ManyToManyEntityDefBuilder()
        init?.invoke(manyToManyBuilder)

        return manyToManyEntity(
            packageName,
            entityBaseName,
            recordVersionHistory = false,
            versioned = false,
            deletable = Deletable.TRUE,
            allowDeleteAll = AllowDeleteAll.FALSE,
            allowFindAll = AllowFindAll.FALSE,
            withHandcodedDao,
            withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
            leftEntity,
            rightEntity,
            pkAndNameFieldName
        ) {

            manyToManyBuilder.description?.let { description(it.value) }

            manyToManyBuilder.effectiveRangeDef?.let { rangeDef ->
                if (rangeDef.dateType == EffectiveRangeDateType.TIMESTAMP)
                    withEffectiveTimestamps(managedBy = rangeDef.managedBy)
                else
                    withEffectiveLocalDates(managedBy = rangeDef.managedBy)
            }

        }

    }


    protected fun manyToManyEntity(
        packageName: String,
        entityBaseName: String,
        recordVersionHistory: Boolean = false,
        versioned: Boolean = false,
        deletable: Deletable = Deletable.FALSE,
        allowDeleteAll: AllowDeleteAll = AllowDeleteAll.FALSE,
        allowFindAll: AllowFindAll = AllowFindAll.FALSE,
        withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
        withHandCodedEntityDao: WithHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
        leftEntity: ReferencedEntity,
        rightEntity: ReferencedEntity,
        pkAndNameFieldName: String? = null,
        init: (EntityDefBuilder.() -> Unit)? = null
    ): ManyToManyEntityDef {

        val builder = EntityDefBuilder(
            PackageName(packageName),
            EntityBaseName(entityBaseName),
            IsDeltaEntity(value = false),
            lookupFieldReaderByFieldType,
            lookupFieldWriterByFieldType,
            WithVersionHistory(recordVersionHistory),
            Versioned(versioned || recordVersionHistory),
            deletable,
            allowDeleteAll,
            allowFindAll,
            pkAndNameFieldName,
            withHandcodedDao,
            withHandCodedEntityDao,
            defaultSchemaName,
            isManyToManyJoinEntity = true
        )

        builder.foreignKey(leftEntity.fieldName, leftEntity.entityDef) {
            fieldDisplayName(leftEntity.displayName)
            if (leftEntity.editableByUser.value) {
                editableByUser()
            }
            if (leftEntity.creatableByUser.value == false) {
                notCreatableByUser()
            }
        }

        builder.foreignKey(rightEntity.fieldName, rightEntity.entityDef) {
            fieldDisplayName(rightEntity.displayName)
            if (rightEntity.editableByUser.value) {
                editableByUser()
            }
            if (rightEntity.creatableByUser.value == false) {
                notCreatableByUser()
            }
        }

        builder.index {
            withFieldAscending(leftEntity.fieldName)
        }

        builder.index {
            withFieldAscending(rightEntity.fieldName)
        }

        init?.invoke(builder)
        val entityDef = builder.build()

        if (entityDef.withVersionHistory.value && entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
            throw ModelDefinitionException(
                "manyToManyEntity '$entityBaseName': recordVersionHistory is not supported for joins with effective timestamps"
            )
        }

        entityDefs.add(entityDef)

        this.rowMapperDefs.add(leftEntity.entityDef.entityPkAndNameDef.rowMapperDef)
        this.rowMapperDefs.add(rightEntity.entityDef.entityPkAndNameDef.rowMapperDef)

        val manyToManyEntityDef = ManyToManyEntityDef(entityDef, leftEntity, rightEntity)

        manyToManyAssociationsByEntityName
            .getOrPut(leftEntity.entityDef.entityBaseName) { mutableListOf() }
            .add(manyToManyEntityDef)

        manyToManyAssociationsByEntityName
            .getOrPut(rightEntity.entityDef.entityBaseName) { mutableListOf() }
            .add(manyToManyEntityDef)

        return manyToManyEntityDef

    }


    protected fun fixedWidthFileStagingEntity(
        packageName: String,
        entityBaseName: String,
        withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
        withHandcodedEntityDao: WithHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
        init: FixedWidthFileStagingEntityDefBuilder.() -> Unit
    ): EntityDef {

        val builder = FixedWidthFileStagingEntityDefBuilder(
            PackageName(packageName),
            EntityBaseName(entityBaseName),
            withHandcodedDao,
            withHandcodedEntityDao,
            this.defaultSchemaName,
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType
        )


        builder.init()
        val def = builder.build()
        this.entityDefs.add(def)
        return def

    }


    protected fun entity(
        packageName: String,
        entityBaseName: String,
        recordVersionHistory: Boolean = false,
        versioned: Boolean = false,
        isDeltaEntity: Boolean = false,
        deletable: Deletable = Deletable.FALSE,
        allowDeleteAll: AllowDeleteAll = AllowDeleteAll.FALSE,
        allowFindAll: AllowFindAll = AllowFindAll.FALSE,
        withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
        withHandcodedEntityDao: WithHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
        nameFieldForPkAndNameDto: String? = null,
        init: EntityDefBuilder.() -> Unit
    ): EntityDef {

        val builder = EntityDefBuilder(
            PackageName(packageName),
            EntityBaseName(entityBaseName),
            IsDeltaEntity(isDeltaEntity),
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType,
            WithVersionHistory(recordVersionHistory),
            Versioned(versioned || recordVersionHistory),
            deletable,
            allowDeleteAll,
            allowFindAll,
            nameFieldForPkAndNameDto,
            withHandcodedDao,
            withHandcodedEntityDao,
            this.defaultSchemaName
        )

        builder.init()

        val entityDef = builder.build()
        this.entityDefs.add(entityDef)
        return entityDef

    }


    protected fun dataClass(
        packageName: String,
        dataClassName: String,
        init: DataClassDefBuilder.() -> Unit
    ): DataClassDef {

        val builder = DataClassDefBuilder(
            PackageName(packageName),
            DataClassName(dataClassName),
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType
        )

        builder.init()
        val def = builder.build()
        this.dataClassDefs.add(def)
        return def

    }


    protected fun fieldListOf(fieldType: FieldType): ListFieldType {

        return FieldTypes.list(fieldType)

    }


    protected fun fieldSetOf(fieldType: FieldType): SetFieldType {

        return FieldTypes.set(fieldType)

    }


    protected fun mapOfString(): FieldTypes.MapFieldTypeBuilder {

        return FieldTypes.MapFieldTypeBuilder(FieldTypes.string)

    }


    protected fun fieldMapOf(keyFieldType: FieldType): FieldTypes.MapFieldTypeBuilder {

        return FieldTypes.MapFieldTypeBuilder(keyFieldType)

    }


    protected fun fieldMapOf(keyFieldType: StringTypeDef): FieldTypes.MapFieldTypeBuilder {

        return FieldTypes.MapFieldTypeBuilder(FieldTypes.stringType(keyFieldType))

    }


    protected fun fieldListOf(enumDef: EnumDef): ListFieldType {

        return FieldTypes.list(FieldTypes.enum(enumDef))

    }


    protected fun fieldSetOf(enumDef: EnumDef): SetFieldType {

        return FieldTypes.set(FieldTypes.enum(enumDef))

    }


    protected fun fieldListOf(responseDtoDef: SimpleResponseDtoDef): ListFieldType {

        return FieldTypes.list(FieldTypes.responseDto(responseDtoDef))

    }


    protected fun fieldListOf(stringTypeDef: StringTypeDef): ListFieldType {

        return FieldTypes.list(FieldTypes.stringType(stringTypeDef))

    }


    protected fun fieldListOf(stringValueClassDef: StringValueClassDef): ListFieldType {

        return FieldTypes.list(FieldTypes.stringValueClass(stringValueClassDef))

    }


    protected fun fieldListOf(dataClassDef: DataClassDef): ListFieldType {

        return FieldTypes.list(FieldTypes.dataClass(dataClassDef))

    }


    protected fun enumValue(
        name: String,
        description: String? = null,
        displayName: String? = null,
        isDefaultFormValue: Boolean = false
    ): EnumValueDef {

        return EnumValueDef(
            name,
            description?.let { Description(it) },
            displayName?.let { DisplayName(it) },
            isDefaultFormValue
        )

    }


    protected fun entityCreateHtmlForm(
        entityDef: EntityDef,
        init: (EntityCreateHtmlFormDefBuilder.() -> Unit)? = null
    ): EntityHtmlFormDef {

        val builder = EntityCreateHtmlFormDefBuilder(entityDef)
        init?.invoke(builder)
        val def = builder.build()
        this.entityCreateHtmlFormDefs.add(def)
        return def

    }


    protected fun requestDtoHtmlForm(
        requestDtoDef: RequestDtoDef,
        init: RequestDtoHtmlFormDefBuilder.() -> Unit
    ): HtmlFormDef {

        val builder = RequestDtoHtmlFormDefBuilder(requestDtoDef)
        builder.init()
        val def = builder.build()
        this.requestDtoHtmlFormDefs.add(def)
        return def


    }


    protected fun angularForm(
        requestDtoDef: RequestDtoDef,
        init: AngularFormDefBuilder.() -> Unit
    ): AngularFormDef {

        val builder = AngularFormDefBuilder(requestDtoDef, AngularComponentBaseName(requestDtoDef.dtoBaseName.value))
        builder.init()
        val def = builder.build()
        this.angularFormDefs.add(def)

        return def

    }


    protected fun requestDto(
        packageName: String,
        dtoBaseName: String,
        requestMappingPath: String? = null,
        moduleName: ModuleName? = null,
        withGeneratedEndpoint: Boolean = true,
        init: RequestDtoDefBuilder.() -> Unit
    ): RequestDtoDef {

        val builder = RequestDtoDefBuilder(
            packageName = PackageName(packageName),
            dtoBaseName = DtoBaseName(dtoBaseName),
            requestMappingPath = requestMappingPath,
            moduleName = moduleName,
            withGeneratedEndpoint = WithGeneratedEndpoint(withGeneratedEndpoint)
        )

        builder.init()
        val def = builder.build()
        this.requestDtoDefs.add(def)
        return def

    }


    protected fun formModel(
        packageName: String,
        className: String,
        init: FormModelDefBuilder.() -> Unit
    ): FormModelDef {

        val builder = FormModelDefBuilder(PackageName(packageName), FormModelClassName(className))
        builder.init()
        val def = builder.build()
        this.formModelDefs.add(def)
        return def

    }


    protected fun responseDto(
        packageName: String,
        baseName: String,
        init: ResponseDtoDefBuilder.() -> Unit
    ): ResponseDtoDef {

        val builder = ResponseDtoDefBuilder(
            PackageName(packageName),
            DtoBaseName(baseName),
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType
        )

        builder.init()
        val def = builder.build()
        this.responseDtoDefs.add(def)
        return def

    }


    protected fun simpleResponseDto(
        packageName: String,
        baseName: String,
        init: SimpleResponseDtoDefBuilder.() -> Unit
    ): SimpleResponseDtoDef {

        val builder = SimpleResponseDtoDefBuilder(
            PackageName(packageName),
            DtoBaseName(baseName),
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType
        )

        builder.init()
        val def = builder.build()
        this.simpleResponseDtoDefs.add(def)
        return def

    }


    protected fun hazelcastDto(
        packageName: String,
        baseName: String,
        init: HazelcastDtoDefBuilder.() -> Unit
    ): HazelcastDtoDef {

        val builder = HazelcastDtoDefBuilder(
            PackageName(packageName),
            DtoBaseName(baseName),
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType
        )

        builder.init()
        val def = builder.build()
        this.hazelcastDtoDefs.add(def)
        return def

    }


    protected fun searchableDto(
        packageName: String,
        baseName: String,
        entityDef: EntityDef,
        withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.FALSE,
        withGeneratedTypescriptService: WithGeneratedTypescriptService = WithGeneratedTypescriptService.TRUE,
        withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE,
        withGeneratedDto: WithGeneratedDto = WithGeneratedDto.FALSE,
        searchModelType: SearchModelType = SearchModelType.default(),
        init: SearchableDtoDefBuilder.() -> Unit
    ): SearchableDtoDef {

        val builder = SearchableDtoDefBuilder(
            entityDef,
            PackageName(packageName),
            DtoBaseName(baseName),
            withGeneratedEndpoint,
            withGeneratedTypescriptService,
            withGeneratedFindAllFunction,
            withGeneratedDto,
            searchModelType,
            this.lookupFieldReaderByFieldType,
            this.lookupFieldWriterByFieldType
        )

        builder.init()

        val def = builder.build()
        this.searchableDtoDefs.add(def)

        return def

    }


    protected fun blotterPage(
        blotterDef: BlotterDef,
        init: (BlotterPageDefBuilder.() -> Unit)? = null
    ): BlotterPageDef {

        val builder = BlotterPageDefBuilder(blotterDef)
        init?.invoke(builder)
        val def = builder.build()
        this.blotterPageDefs.add(def)
        return def

    }


    protected fun blotter(
        searchableDtoDef: SearchableDtoDef,
        entityCreatePageDef: EntityCreatePageDef? = null,
        entityDetailViewPageDef: EntityDetailViewDef? = null,
        entityEditPageDef: EntityEditPageDef? = null,
        disableRendering: Boolean = false,
        withPreAuthorize: WithPreAuthorize? = null,
        withGeneratedDto: WithGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedTypescriptService: WithGeneratedTypescriptService = WithGeneratedTypescriptService.TRUE,
        withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE,
        searchModelType: SearchModelType = SearchModelType.default(),
        init: BlotterDefBuilder.() -> Unit
    ): BlotterDef {

        if (entityCreatePageDef != null) {

            searchableDtoDef.dtoRootEntityDef.entityCrudApiDef?.createApiDef
                ?: throw RuntimeException("blotter with an add button must be backed by an entity with a Create API. searchableDtoDef = ${searchableDtoDef.searchDtoDef.dtoBaseName}.")

        }

        val builder = BlotterDefBuilder(
            searchableDtoDef.packageName,
            searchableDtoDef.dtoBaseName,
            blotterSourceDef = BlotterSearchableDtoSourceDef(searchableDtoDef),
            entityCreatePageDef = entityCreatePageDef,
            entityEditPageDef = entityEditPageDef,
            entityDetailViewDef = entityDetailViewPageDef,
            disableRendering = disableRendering,
            withGeneratedDto = withGeneratedDto,
            withGeneratedFindAllFunction = withGeneratedFindAllFunction,
            withGeneratedEndpoint = withGeneratedEndpoint,
            withGeneratedTypescriptService = withGeneratedTypescriptService,
            withPreAuthorize = withPreAuthorize,
            searchModelType = searchModelType
        )

        searchableDtoDef.moduleName?.let { builder.moduleName(it.value) }

        builder.init()
        val def = builder.build()
        this.blotterDefs.add(def)
        return def

    }


    protected fun blotter(
        esDocDef: EsDocDef,
        entityCreatePageDef: EntityCreatePageDef? = null,
        entityDetailViewPageDef: EntityDetailViewDef? = null,
        entityEditPageDef: EntityEditPageDef? = null,
        disableRendering: Boolean = false,
        withGeneratedDto: WithGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedTypescriptService: WithGeneratedTypescriptService = WithGeneratedTypescriptService.TRUE,
        withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE,
        withPreAuthorize: WithPreAuthorize? = null,
        searchModelType: SearchModelType = SearchModelType.default(),
        init: BlotterDefBuilder.() -> Unit
    ): BlotterDef {

        val builder = BlotterDefBuilder(
            esDocDef.packageName,
            esDocDef.esDocBaseName,
            entityCreatePageDef = entityCreatePageDef,
            entityEditPageDef = entityEditPageDef,
            entityDetailViewDef = entityDetailViewPageDef,
            disableRendering = disableRendering,
            searchModelType = searchModelType,
            withGeneratedDto = withGeneratedDto,
            withPreAuthorize = withPreAuthorize,
            withGeneratedEndpoint = withGeneratedEndpoint,
            withGeneratedTypescriptService = withGeneratedTypescriptService,
            withGeneratedFindAllFunction = withGeneratedFindAllFunction,
            blotterSourceDef = BlotterEsDocSourceDef(esDocDef)
        )

        builder.init()
        val def = builder.build()
        this.blotterDefs.add(def)
        return def

    }


    protected fun entityDetailView(
        entityDef: EntityDef,
        init: (EntityDetailViewDefBuilder.() -> Unit)? = null
    ): EntityDetailViewDef {

        val builder = EntityDetailViewDefBuilder(entityDef)
        init?.invoke(builder)

        val def = builder.build()
        this.entityDetailViewDefs.add(def)

        builder.entityEditPageDef?.let { this.entityEditPageDefs.add(it) }

        return def

    }


    protected fun entityEditPage(
        entityDef: EntityDef,
        init: (EntityEditPageDefBuilder.() -> Unit)? = null
    ): EntityEditPageDef {

        val builder = EntityEditPageDefBuilder(entityDef)
        init?.invoke(builder)

        val def = builder.build()

        this.entityEditPageDefs.add(def)

        return def

    }


    protected fun entityCreatePage(
        entityDef: EntityDef,
        init: (EntityCreatePageDefBuilder.() -> Unit)? = null
    ): EntityCreatePageDef {

        val builder = EntityCreatePageDefBuilder(entityDef)
        init?.invoke(builder)

        val def = builder.build()

        this.entityCreatePageDefs.add(def)

        return def

    }


    protected fun typeahead(
        packageName: String,
        typeaheadName: String,
        entityDef: EntityDef? = null,
        sortFieldName: String,
        searchTermFieldName: String,
        indexVersion: Int = 1,
        withHandCodedEsDocRepo: WithHandCodedEsDocRepo = WithHandCodedEsDocRepo.FALSE,
        init: TypeaheadDefBuilder.() -> Unit
    ): TypeaheadDef {

        val builder = TypeaheadDefBuilder(
            PackageName(packageName),
            TypeaheadName(typeaheadName),
            entityDef,
            sortFieldName,
            searchTermFieldName,
            indexVersion,
            withHandCodedEsDocRepo
        )

        builder.init()
        val def = builder.build()
        this.typeaheadDefs.add(def)
        return def

    }


    protected fun typeaheadFromEntity(
        packageName: String,
        typeaheadName: String,
        entityDef: EntityDef? = null,
        sortFieldName: String,
        searchTermFieldName: String,
        indexVersion: Int = 1,
        withHandCodedEsDocRepo: WithHandCodedEsDocRepo = WithHandCodedEsDocRepo.FALSE,
        init: TypeaheadDefBuilder.() -> Unit
    ): TypeaheadDef {

        val builder = TypeaheadDefBuilder(
            PackageName(packageName),
            TypeaheadName(typeaheadName),
            entityDef,
            sortFieldName,
            searchTermFieldName,
            indexVersion,
            withHandCodedEsDocRepo
        )

        builder.init()
        val def = builder.build()
        this.typeaheadDefs.add(def)
        return def

    }


    protected fun esDoc(
        packageName: String,
        esDocName: String,
        indexBaseName: String? = null,
        esDocVersion: Int = 1,
        indexDescription: String,
        renderFieldEnum: Boolean = false,
        generateRefreshJob: Boolean = true,
        disableRendering: Boolean = false,
        init: EsDocDefBuilder.() -> Unit
    ): EsDocDef {

        val indexBaseNameToUse = indexBaseName ?: StringFunctions.toSnakeCase(esDocName)

        val builder = EsDocDefBuilder(
            PackageName(packageName),
            DtoBaseName(esDocName),
            ElasticIndexBaseName(indexBaseNameToUse),
            esDocVersion,
            Description(indexDescription),
            renderFieldEnum,
            generateRefreshJob,
            disableRendering
        )

        builder.init()
        val def = builder.build()
        this.esDocDefs.add(def)
        return def

    }


    fun authority(
        name: String,
        init: (AuthorityBuilder.() -> Unit)? = null
    ): AuthorityDef {

        val authorityBuilder = AuthorityBuilder(name)
        init?.invoke(authorityBuilder)
        val authorityDef = authorityBuilder.build()
        this.authorityDefs.add(authorityDef)
        return authorityDef

    }


    @MaiaDslMarker
    class AuthorityBuilder(val name: String) {


        var description: String? = null


        fun build(): AuthorityDef {
            return AuthorityDef(
                this.name,
                this.description?.let { Description(it) }
            )
        }


    }


}
