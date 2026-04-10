package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AbstractSearchableDtoFieldDef
import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.EntityAndField
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.FieldPath
import org.maiaframework.gen.spec.definition.JoinEntityDef
import org.maiaframework.gen.spec.definition.JoinType
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.ManyToManySearchableDtoFieldDef
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.ResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithProvidedFieldConverter
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability
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
    private val fieldDefs = mutableListOf<AbstractSearchableDtoFieldDef>()
    private var moduleName: ModuleName? = null
    private var withPreAuthorize: WithPreAuthorize? = null
    private var withProvidedFieldConverter = WithProvidedFieldConverter.FALSE
    private var generateFindById: GenerateFindById = GenerateFindById.FALSE


    fun build(): SearchableDtoDef {

        return SearchableDtoDef(
            this.rootEntityDef,
            this.dtoBaseName,
            this.moduleName,
            this.packageName,
            this.rootEntityDef.tableName,
            this.fieldDefs,
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


    fun manyToManyJoin(joinEntityDef: ManyToManyEntityDef, joinType: JoinType = JoinType.INNER) {

        this.manyToManyJoinEntityDefs.add(JoinEntityDef(joinEntityDef.entityDef, joinType))

    }


    fun manyToManyField(fieldName: String, manyToManyEntityDef: ManyToManyEntityDef) {

        this.manyToManyJoinEntityDefs.add(JoinEntityDef(manyToManyEntityDef.entityDef, JoinType.INNER))

        val otherEntityDef = `find the Entity on the other side of`(manyToManyEntityDef)

        val classFieldDef = ClassFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.list(FieldTypes.pkAndName(otherEntityDef.entityPkAndNameDef)),
        ).build()

        val responseDtoFieldDef = ResponseDtoFieldDef(
            ClassFieldName(fieldName),
            tableColumnName = TableColumnName("BOGUS"), // TODO this.entityAndField.databaseColumnName,
            fieldType = FieldTypes.list(FieldTypes.pkAndName(otherEntityDef.entityPkAndNameDef)),
            nullability = Nullability.NOT_NULLABLE,
            isMasked = false,
            caseSensitive = CaseSensitive.FALSE,
            fieldReaderParameterizedType = null,
            fieldWriterParameterizedType = null
        )

        val fieldDef = ManyToManySearchableDtoFieldDef(
            classFieldDef,
            sortIndexAndDirection = null // TODO
        )

        fieldDefs.add(fieldDef)

    }


    private fun `find the Entity on the other side of`(manyToManyEntityDef: ManyToManyEntityDef): EntityDef {

        return when (this.rootEntityDef) {

            manyToManyEntityDef.leftEntity.entityDef -> manyToManyEntityDef.rightEntity.entityDef

            manyToManyEntityDef.rightEntity.entityDef -> manyToManyEntityDef.leftEntity.entityDef

            else -> throw IllegalArgumentException("The provided manyToManyEntityDef (${manyToManyEntityDef.entityDef.entityBaseName}) does not reference this root entity (${this.rootEntityDef.entityBaseName}).")

        }

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
        fieldDefs.add(builder.build())

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


}
