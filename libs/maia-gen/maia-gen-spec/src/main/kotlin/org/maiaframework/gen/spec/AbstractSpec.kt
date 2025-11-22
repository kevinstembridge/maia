package org.maiaframework.gen.spec

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec.AuthoritiesBuilder.AuthorityBuilder
import org.maiaframework.gen.spec.definition.AddButtonDef
import org.maiaframework.gen.spec.definition.AngularComponentBaseName
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AnnotationDefs
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.BooleanValueClassDef
import org.maiaframework.gen.spec.definition.CrudTableDef
import org.maiaframework.gen.spec.definition.DataClassDef
import org.maiaframework.gen.spec.definition.DataClassName
import org.maiaframework.gen.spec.definition.DataSourceType
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableSourceDef
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.EntityHtmlFormDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EnumValueDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.FormModelClassName
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.HazelcastDtoDef
import org.maiaframework.gen.spec.definition.HtmlFormDef
import org.maiaframework.gen.spec.definition.ElasticIndexBaseName
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.LongTypeDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ModelDefProvider
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.ResponseDtoDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.TypeaheadName
import org.maiaframework.gen.spec.definition.builders.AngularFormDefBuilder
import org.maiaframework.gen.spec.definition.builders.BooleanTypeDefBuilder
import org.maiaframework.gen.spec.definition.builders.BooleanValueClassDefBuilder
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.CrudTableDefBuilder
import org.maiaframework.gen.spec.definition.builders.DataClassDefBuilder
import org.maiaframework.gen.spec.definition.builders.DtoHtmlTableDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityCreateHtmlFormDefBuilder
import org.maiaframework.gen.spec.definition.builders.EntityDefBuilder
import org.maiaframework.gen.spec.definition.builders.EnumDefBuilder
import org.maiaframework.gen.spec.definition.builders.EsDocDefBuilder
import org.maiaframework.gen.spec.definition.builders.FixedWidthFileStagingEntityDefBuilder
import org.maiaframework.gen.spec.definition.builders.FormModelDefBuilder
import org.maiaframework.gen.spec.definition.builders.HazelcastDtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.IntTypeDefBuilder
import org.maiaframework.gen.spec.definition.builders.LongTypeDefBuilder
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
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.Versioned
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithHandCodedDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEntityDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEsDocRepo
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithVersionHistory
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.Uqcn
import org.maiaframework.lang.text.StringFunctions


abstract class AbstractSpec protected constructor(
    private val appKey: AppKey,
    defaultSchemaName: SchemaName? = null,
    private val defaultDatabaseType: DatabaseType = DatabaseType.JDBC,
    basePackageName: PackageName? = null
) : ModelDefProvider {


    private var authoritiesDef: AuthoritiesDef? = null
    private val entityDefs = mutableListOf<EntityDef>()
    private val dataClassDefs = mutableListOf<DataClassDef>()
    private val requestDtoDefs = mutableListOf<RequestDtoDef>()
    private val formModelDefs = mutableListOf<FormModelDef>()
    private val responseDtoDefs = mutableListOf<ResponseDtoDef>()
    private val simpleResponseDtoDefs = mutableListOf<SimpleResponseDtoDef>()
    private val hazelcastDtoDefs = mutableListOf<HazelcastDtoDef>()
    private val searchableDtoDefs = mutableListOf<SearchableDtoDef>()
    private val dtoHtmlTableDefs = mutableListOf<DtoHtmlTableDef>()
    private val crudTableDefs = mutableListOf<CrudTableDef>()
    private val entityCreateHtmlFormDefs = mutableListOf<EntityHtmlFormDef>()
    private val requestDtoHtmlFormDefs = mutableListOf<HtmlFormDef>()
    private val angularFormDefs = mutableListOf<AngularFormDef>()
    private val enumDefs = mutableListOf<EnumDef>()
    private val stringTypeDefs = mutableListOf<StringTypeDef>()
    private val booleanValueClassDefs = mutableListOf<BooleanValueClassDef>()
    private val stringValueClassDefs = mutableListOf<StringValueClassDef>()
    private val intTypeDefs = mutableListOf<IntTypeDef>()
    private val longTypeDefs = mutableListOf<LongTypeDef>()
    private val booleanTypeDefs = mutableListOf<BooleanTypeDef>()
    private val typeaheadDefs = mutableListOf<TypeaheadDef>()
    private val esDocDefs = mutableListOf<EsDocDef>()
    private val rootEntityHierarchies = mutableListOf<EntityHierarchy>()
    private val fieldReadersByFieldType = mutableMapOf<FieldType, ParameterizedType>()
    private val fieldWritersByFieldType = mutableMapOf<FieldType, ParameterizedType>()
    private val basePackageName = basePackageName ?: PackageName(appKey.value.lowercase())
    private val defaultSchemaName = defaultSchemaName ?: SchemaName(appKey.value)


    private val lookupFieldReaderByFieldType = { fieldType: FieldType -> this.fieldReadersByFieldType[fieldType] }
    private val lookupFieldWriterByFieldType = { fieldType: FieldType -> this.fieldWritersByFieldType[fieldType] }


    override val modelDef: ModelDef
        get() {

            populateEntityHierarchy()

            return ModelDef(
                this.appKey,
                this.rootEntityHierarchies,
                this.authoritiesDef,
                this.formModelDefs,
                this.entityCreateHtmlFormDefs,
                this.requestDtoDefs,
                this.responseDtoDefs,
                this.simpleResponseDtoDefs,
                this.hazelcastDtoDefs,
                this.searchableDtoDefs,
                this.dtoHtmlTableDefs,
                this.requestDtoHtmlFormDefs,
                this.angularFormDefs,
                this.dataClassDefs,
                this.enumDefs,
                this.booleanTypeDefs,
                this.intTypeDefs,
                this.longTypeDefs,
                this.stringTypeDefs,
                this.typeaheadDefs,
                this.crudTableDefs,
                this.esDocDefs,
                buildHazelcastConfigClassDef()
            )

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


    private fun buildHazelcastConfigClassDef(): ClassDef {

        val fqcn = this.basePackageName.plusSubPackage("hazelcast").uqcn("${appKey.firstToUpper()}HazelcastConfig")
        return aClassDef(fqcn)
            .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
            .build()

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
        databaseType: DatabaseType? = null,
        leftEntity: ReferencedEntity,
        rightEntity: ReferencedEntity,
        idAndNameFieldName: String? = null,
        init: (EntityDefBuilder.() -> Unit)? = null
    ): EntityDef {

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
            idAndNameFieldName,
            withHandcodedDao,
            withHandCodedEntityDao,
            defaultSchemaName,
            databaseType ?: defaultDatabaseType
        )

        builder.foreignKey(leftEntity.fieldName, leftEntity.entityDef) {
            fieldDisplayName(leftEntity.displayName)
            if (leftEntity.editableByUser.value) {
                editableByUser()
            }
        }

        builder.foreignKey(rightEntity.fieldName, rightEntity.entityDef) {
            fieldDisplayName(rightEntity.displayName)
            if (rightEntity.editableByUser.value) {
                editableByUser()
            }
        }

        init?.invoke(builder)
        val entityDef = builder.build()
        entityDefs.add(entityDef)

        return entityDef

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
        idAndNameFieldName: String? = null,
        databaseType: DatabaseType? = null,
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
            idAndNameFieldName,
            withHandcodedDao,
            withHandcodedEntityDao,
            this.defaultSchemaName,
            databaseType ?: this.defaultDatabaseType
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


    protected fun mapOfStringToString(): MapFieldType {

        return FieldTypes.MapFieldTypeBuilder(FieldTypes.string).to(FieldTypes.string)

    }


    protected fun mapOfStringToAny(): MapFieldType {

        return FieldTypes.MapFieldTypeBuilder(FieldTypes.string).to(Fqcn.ANY)

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


    protected fun fieldListOf(dataClassDef: DataClassDef): ListFieldType {

        return FieldTypes.list(FieldTypes.dataClass(dataClassDef))

    }


    protected fun enumValue(name: String): EnumValueDef {

        return EnumValueDef(name)

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


    protected fun hazelcastDtoDef(
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


    protected fun searchableEntityDef(
        packageName: String,
        baseName: String,
        entityDef: EntityDef,
        withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.FALSE,
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


    protected fun crudTableDef(
        dtoDef: DtoHtmlTableDef,
        entityCrudApiDef: EntityCrudApiDef,
        init: (CrudTableDefBuilder.() -> Unit)? = null
    ): CrudTableDef {

        val builder = CrudTableDefBuilder(dtoDef, entityCrudApiDef)
        init?.invoke(builder)
        val def = builder.build()
        this.crudTableDefs.add(def)
        return def

    }


    protected fun dtoHtmlTable(
        searchableDtoDef: SearchableDtoDef,
        withAddButton: Boolean = false,
        disableRendering: Boolean = false,
        withPreAuthorize: WithPreAuthorize? = null,
        withGeneratedDto: WithGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE,
        searchModelType: SearchModelType = SearchModelType.default(),
        init: DtoHtmlTableDefBuilder.() -> Unit
    ): DtoHtmlTableDef {

        val addButtonDef = if (withAddButton) {

            val createApiDef = searchableDtoDef.dtoRootEntityDef.entityCrudApiDef?.createApiDef
                ?: throw RuntimeException("dtoHtmlTable with an add button must be backed by an entity with a Create API. searchableDtoDef = ${searchableDtoDef.searchDtoDef.dtoBaseName}.")

            val authority = createApiDef.crudApiDef.authority

            AddButtonDef(authority)

        } else {
            null
        }

        val builder = DtoHtmlTableDefBuilder(
            searchableDtoDef.packageName,
            searchableDtoDef.dtoBaseName,
            dtoHtmlTableSourceDef = DtoHtmlTableSourceDef.of(searchableDtoDef),
            fieldSupplier = { fieldName -> searchableDtoDef.findFieldByPath(fieldName).classFieldDef },
            addButtonDef = addButtonDef,
            disableRendering = disableRendering,
            dataSourceType = DataSourceType.DATABASE,
            withGeneratedDto = withGeneratedDto,
            withGeneratedFindAllFunction = withGeneratedFindAllFunction,
            withGeneratedEndpoint = withGeneratedEndpoint,
            withPreAuthorize = withPreAuthorize,
            searchModelType = searchModelType
        )

        searchableDtoDef.moduleName?.let { builder.moduleName(it.value) }

        builder.init()
        val def = builder.build()
        this.dtoHtmlTableDefs.add(def)
        return def

    }


    protected fun dtoHtmlTable(
        esDocDef: EsDocDef,
        disableRendering: Boolean = false,
        withGeneratedDto: WithGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE,
        withPreAuthorize: WithPreAuthorize? = null,
        searchModelType: SearchModelType = SearchModelType.default(),
        init: DtoHtmlTableDefBuilder.() -> Unit
    ): DtoHtmlTableDef {

        val builder = DtoHtmlTableDefBuilder(
            esDocDef.packageName,
            esDocDef.esDocBaseName,
            fieldSupplier = { fieldPath -> esDocDef.findFieldByPath(fieldPath) },
            addButtonDef = null,
            disableRendering = disableRendering,
            dataSourceType = DataSourceType.ELASTIC_SEARCH,
            searchModelType = searchModelType,
            withGeneratedDto = withGeneratedDto,
            withPreAuthorize = withPreAuthorize,
            withGeneratedEndpoint = withGeneratedEndpoint,
            withGeneratedFindAllFunction = withGeneratedFindAllFunction,
            dtoHtmlTableSourceDef = DtoHtmlTableSourceDef.of(esDocDef)
        )

        builder.init()
        val def = builder.build()
        this.dtoHtmlTableDefs.add(def)
        return def

    }


    protected fun typeahead(
        packageName: String,
        typeaheadName: String,
        entityDef: EntityDef? = null,
        idFieldName: String,
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
            idFieldName,
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
        idFieldName: String,
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
            idFieldName,
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
        return authorityBuilder.build()

    }


    fun authorities(
        packageName: String,
        className: String,
        init: AuthoritiesBuilder.() -> Unit
    ): AuthoritiesDef {

        if (this.authoritiesDef != null) {
            throw IllegalStateException("The authorities function can only be called once.")
        }

        val builder = AuthoritiesBuilder(PackageName(packageName), className)
        builder.init()
        val def = builder.build()
        this.authoritiesDef = def
        return def

    }


    class AuthoritiesBuilder(private val packageName: PackageName, private val className: String) {


        private val authorityDefs = mutableListOf<AuthorityDef>()


        private val authorityBuilders = mutableListOf<AuthorityBuilder>()


        fun build(): AuthoritiesDef {

            val enumUqcn = Uqcn(className)
            val fqcn = packageName.uqcn(enumUqcn)

            val authorityDefsFromBuilders = this.authorityBuilders.map { it.build() }

            val enumValueDefs = this.authorityDefs.plus(authorityDefsFromBuilders).map {
                EnumValueDef(it.name, it.description)
            }

            val enumDef = EnumDef(
                fqcn,
                enumValueDefs,
                isProvided = false,
                withTypescript = true,
                withEnumSelectionOptions = true
            )

            return AuthoritiesDef(enumDef)

        }


        fun authority(authorityDef: AuthorityDef) {

            this.authorityDefs.add(authorityDef)

        }


        fun authority(
            name: String,
            init: (AuthorityBuilder.() -> Unit)? = null
        ): AuthorityBuilder {

            val builder = AuthorityBuilder(name)

            authorityBuilders.add(builder)

            if (init != null) {
                builder.init()
            }

            return builder

        }


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


    data class ReferencedEntity(
        val fieldName: String,
        val displayName: String,
        val entityDef: EntityDef,
        val editableByUser: IsEditableByUser = IsEditableByUser.FALSE
    )


}
