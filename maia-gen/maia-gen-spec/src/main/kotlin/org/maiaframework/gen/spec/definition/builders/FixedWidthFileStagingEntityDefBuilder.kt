package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.definition.AngularFormSystem
import org.maiaframework.gen.spec.definition.ConfigurableSchemaPropertyName
import org.maiaframework.gen.spec.definition.CrudDef
import org.maiaframework.gen.spec.definition.DataRowHeaderName
import org.maiaframework.gen.spec.definition.DataRowStagingEntityFieldDef
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.SimpleFieldDef
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
import org.maiaframework.gen.spec.definition.flags.Versioned
import org.maiaframework.gen.spec.definition.flags.WithHandCodedDao
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEntityDao
import org.maiaframework.gen.spec.definition.flags.WithVersionHistory
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.jdbc.TableName


@MaiaDslMarker
class FixedWidthFileStagingEntityDefBuilder(
    private val packageName: PackageName,
    val entityBaseName: EntityBaseName,
    private val withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
    private val withHandcodedEntityDao: WithHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
    defaultSchemaName: SchemaName,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?,
) {

    private val databaseType = DatabaseType.JDBC

    private val entityFieldDefBuilders = mutableListOf<FixedWidthStagingEntityFieldDefBuilder>()
    private var tableName: TableName? = null
    private var configurableSchemaPropertyName: ConfigurableSchemaPropertyName? = null
    private var schemaName: SchemaName = defaultSchemaName
    private var moduleName: ModuleName? = null
    private var daoHasSpringAnnotation: DaoHasSpringAnnotation = DaoHasSpringAnnotation.TRUE
    private var entityDaoHasSpringAnnotation: EntityDaoHasSpringAnnotation = EntityDaoHasSpringAnnotation.TRUE


    fun build(): EntityDef {

        val stagingEntityFieldDefs = buildFieldDefs()

        val entityFieldDefs = stagingEntityFieldDefs
            .map { it.entityFieldDef }
            .plus(fileStorageIdField())
            .plus(EntityDef.idFieldDef(entityBaseName, packageName))
            .plus(EntityDef.createdTimestampUtcFieldDef(entityBaseName, packageName))
            .plus(lineNumberField())

        return EntityDef(
            this.entityBaseName,
            this.tableName,
            this.schemaName,
            this.databaseType,
            this.configurableSchemaPropertyName,
            description = null,
            isCappedCollection = IsCappedCollection.FALSE,
            cappedSizeInBytes = null,
            isDeltaEntity = IsDeltaEntity.FALSE,
            superclassEntityDef = null,
            packageName = this.packageName,
            moduleName = this.moduleName,
            entityFieldsNotInherited = entityFieldDefs,
            isAbstract = false,
            typeDiscriminatorOrNull = null,
            withHandCodedDao = this.withHandcodedDao,
            withHandCodedEntityDao = this.withHandcodedEntityDao,
            deletable = Deletable.TRUE,
            daoHasSpringAnnotation = this.daoHasSpringAnnotation,
            entityDaoHasSpringAnnotation = this.entityDaoHasSpringAnnotation,
            allowDeleteAll = AllowDeleteAll.TRUE,
            allowFindAll = AllowFindAll.TRUE,
            providedIndexDefs = emptyList(),
            withVersionHistory = WithVersionHistory.FALSE,
            versioned = Versioned.FALSE,
            isHistoryEntity = false,
            stagingEntityFieldDefs = stagingEntityFieldDefs,
            hasEffectiveTimestamps = HasEffectiveTimestamps.FALSE,
            hasEffectiveLocalDates = HasEffectiveLocalDates.FALSE,
            hasEntityDetailDtoDef = HasEntityDetailDtoDef.FALSE,
            hasSingleEffectiveRecord = HasSingleEffectiveRecord.FALSE,
            cacheableDef = null,
            crudDef = CrudDef.EMPTY,
            angularFormSystem = AngularFormSystem.SIGNAL
        )

    }


    private fun buildFieldDefs(): List<DataRowStagingEntityFieldDef> {

        return entityFieldDefBuilders.map { builder -> builder.build() }

    }


    private fun fileStorageIdField(): EntityFieldDef {

        val builder = EntityFieldDefBuilder(
            ClassFieldName("fileStorageId"),
            FieldTypes.domainId,
            this.entityBaseName,
            this.packageName,
            this.defaultFieldTypeFieldReaderProvider,
            this.defaultFieldTypeFieldWriterProvider
        )

        builder.description("The storage ID of the file that this record was most recently imported from. We use this to identify records that did not exist in the most recent file and can therefore be deleted.")

        return builder.build()

    }


    private fun lineNumberField(): EntityFieldDef {

        val builder = EntityFieldDefBuilder(
            ClassFieldName("lineNumber"),
            FieldTypes.long,
            this.entityBaseName,
            this.packageName,
            this.defaultFieldTypeFieldReaderProvider,
            this.defaultFieldTypeFieldWriterProvider
        )

        builder.description("The number of the line in the input file that contained this record.")

        return builder.build()

    }


    fun tableName(tableName: String): FixedWidthFileStagingEntityDefBuilder {

        this.tableName = TableName(tableName, null)
        return this

    }


    fun moduleName(moduleName: String) {

        this.moduleName = ModuleName.of(moduleName)
        
    }


    fun field(
        fieldName: String,
        dataColumnHeaderName: String,
        fixedWidth: Int,
        expectedFieldType: FieldType = FieldTypes.string,
        expectedFieldNullability: Nullability = Nullability.NULLABLE,
        init: (FixedWidthStagingEntityFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            DataRowHeaderName(dataColumnHeaderName),
            fixedWidth,
            SimpleFieldDef(expectedFieldType, expectedFieldNullability)
        )

        init?.invoke(builder)

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        dataRowHeaderName: DataRowHeaderName,
        fixedWidth: Int,
        expectedFieldType: SimpleFieldDef
    ): FixedWidthStagingEntityFieldDefBuilder {

        return add(
            FixedWidthStagingEntityFieldDefBuilder(
                classFieldName,
                FieldTypes.string,
                dataRowHeaderName,
                fixedWidth,
                this.entityBaseName,
                this.packageName,
                expectedFieldType
            )
        )

    }


    private fun add(builder: FixedWidthStagingEntityFieldDefBuilder): FixedWidthStagingEntityFieldDefBuilder {

        this.entityFieldDefBuilders.add(builder)
        return builder

    }


}
