package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.EntityAndField
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.FieldPath
import org.maiaframework.gen.spec.definition.JoinEntityDef
import org.maiaframework.gen.spec.definition.JoinType
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.SearchableDtoFieldDef
import org.maiaframework.gen.spec.definition.SearchableDtoLookupDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithProvidedFieldConverter
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


@MaiaDslMarker
class SearchableDtoDefBuilder(
    private val rootEntityDef: EntityDef,
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val generatedEndpoint: WithGeneratedEndpoint,
    private val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    private val withGeneratedDto: WithGeneratedDto,
    private val searchModelType: SearchModelType,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {

    private val manyToManyJoinEntityDefs = mutableListOf<JoinEntityDef>()
    private val fieldDefBuilders = mutableListOf<SearchableDtoFieldDefBuilder>()
    private val lookupDefBuilders = mutableListOf<SearchableDtoLookupDefBuilder>()
    private var moduleName: ModuleName? = null
    private var withPreAuthorize: WithPreAuthorize? = null
    private var withProvidedFieldConverter = WithProvidedFieldConverter.FALSE
    private var generateFindById: GenerateFindById = GenerateFindById.FALSE


    fun build(): SearchableDtoDef {

        val fieldDefs = buildFieldDefs()
        val lookupDefs = buildLookupDefs()

        return SearchableDtoDef(
            this.rootEntityDef,
            this.dtoBaseName,
            this.moduleName,
            this.packageName,
            this.rootEntityDef.tableName,
            fieldDefs,
            lookupDefs,
            this.withPreAuthorize,
            this.generatedEndpoint,
            this.withGeneratedFindAllFunction,
            this.withGeneratedDto,
            this.generateFindById,
            this.searchModelType,
            this.withProvidedFieldConverter,
            this.manyToManyJoinEntityDefs.toList()
        )

    }


    private fun buildFieldDefs(): List<SearchableDtoFieldDef> {

        return this.fieldDefBuilders.map { it.build() }

    }


    private fun buildLookupDefs(): List<SearchableDtoLookupDef> {

        return this.lookupDefBuilders.map { it.build() }

    }


    private fun add(builder: SearchableDtoFieldDefBuilder): SearchableDtoFieldDefBuilder {

        this.fieldDefBuilders.add(builder)
        return builder

    }


    private fun add(builder: SearchableDtoLookupDefBuilder): SearchableDtoLookupDefBuilder {

        this.lookupDefBuilders.add(builder)
        return builder

    }


    fun manyToManyJoin(joinEntityDef: EntityDef, joinType: JoinType = JoinType.INNER) {

        this.manyToManyJoinEntityDefs.add(JoinEntityDef(joinEntityDef, joinType))

    }


    fun moduleName(moduleName: String) {

        this.moduleName = ModuleName.of(moduleName)

    }


    fun generateFindByIdStack() {

        this.generateFindById = GenerateFindById.TRUE

    }


    fun field(
        dtoFieldName: String,
        entityFieldPath: String? = null,
        caseSensitive: Boolean = false,
        init: (SearchableDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldPathToUse = entityFieldPath ?: dtoFieldName
        val fieldPath = FieldPath.of(fieldPathToUse)
        val leafEntityAndField = this.rootEntityDef.findFieldByPathOrNull(fieldPath)
            ?: findManyToManyFieldOrNull(fieldPath)
            ?: throw IllegalArgumentException("Cannot find field by path '$fieldPathToUse' on searchableDto $dtoBaseName with fields ${this.rootEntityDef.allEntityFieldsSorted.map { it.classFieldName }}")

        val builder = SearchableDtoFieldDefBuilder(
            ClassFieldName(dtoFieldName),
            leafEntityAndField,
            fieldPath,
            CaseSensitive(caseSensitive),
            defaultFieldTypeFieldReaderProvider,
            defaultFieldTypeFieldWriterProvider
        )

        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    private fun findManyToManyFieldOrNull(fieldPath: FieldPath): EntityAndField? {

        val entityBaseName = EntityBaseName(fieldPath.head())

        return this.manyToManyJoinEntityDefs.firstOrNull { it.entityDef.entityBaseName == entityBaseName }
            ?.entityDef?.findFieldByPath(fieldPath.tail())

    }


    fun withPreAuthorize(expression: String): SearchableDtoDefBuilder {

        this.withPreAuthorize = WithPreAuthorize(expression)
        return this

    }


    fun withProvidedFieldConverter(): SearchableDtoDefBuilder {

        this.withProvidedFieldConverter = WithProvidedFieldConverter.TRUE
        return this

    }


    fun lookup(
        foreignKeyEntityDef: EntityDef,
        localField: String,
        foreignField: String = "id"
    ): SearchableDtoLookupDefBuilder {

        val localFieldClassFieldName = ClassFieldName(localField)
        val foreignFieldDef = foreignKeyEntityDef.findFieldByName(foreignField)

        return add(
            SearchableDtoLookupDefBuilder(
                foreignKeyEntityDef,
                localFieldClassFieldName,
                foreignFieldDef,
                this,
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )

    }


}
