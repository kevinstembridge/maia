package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.DtoSuffix
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.PreAuthorizeExpression
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.RequestDtoFieldDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import java.util.LinkedList


@MaiaDslMarker
class RequestDtoDefBuilder(
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val dtoSuffix: DtoSuffix = DtoSuffix("RequestDto"),
    private val moduleName: ModuleName?,
    private val requestMappingPath: String?,
    private val withGeneratedEndpoint: WithGeneratedEndpoint
) {


    private val fieldDefBuilders = LinkedList<RequestDtoFieldDefBuilder>()


    private var preAuthorizeExpression: PreAuthorizeExpression? = null


    fun build(): RequestDtoDef {

        val fieldDefs = buildFieldDefs()

        return RequestDtoDef(
            packageName = this.packageName,
            dtoBaseName = this.dtoBaseName,
            dtoSuffix = this.dtoSuffix,
            requestMappingPath = this.requestMappingPath,
            moduleName = this.moduleName,
            dtoFieldDefs = fieldDefs,
            preAuthorizeExpression = this.preAuthorizeExpression,
            withGeneratedEndpoint = this.withGeneratedEndpoint
        )

    }


    private fun buildFieldDefs(): List<RequestDtoFieldDef> {

        return this.fieldDefBuilders.flatMap { it.build() }.sorted()

    }


    fun field(
        fieldName: String,
        entityDef: EntityDef,
        entityFieldName: String,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val entityFieldDef = entityDef.findFieldByName(entityFieldName)

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            entityFieldDef.fieldType,
        )

        if (entityFieldDef.classFieldDef.isUnique) {
            builder.unique()
        }

        val databaseIndexDef = entityDef.findUniqueDatabaseIndexDefFor(ClassFieldName(entityFieldName))
        builder.withDatabaseIndexDef(databaseIndexDef)

        builder.withValidationConstraints(entityFieldDef.classFieldDef.validationConstraints)

        init?.invoke(builder)

        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            fieldType
        )

        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        enumDef: EnumDef,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            enumDef
        )

        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            stringTypeDef
        )
        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        stringValueClassDef: StringValueClassDef,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            stringValueClassDef
        )
        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        listFieldType: ListFieldType,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            listFieldType
        )

        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        mapFieldType: MapFieldType,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            mapFieldType
        )

        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun field(
        fieldName: String,
        requestDtoDef: RequestDtoDef,
        init: (RequestDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = RequestDtoFieldDefBuilder(
            ClassFieldName(fieldName),
            requestDtoDef
        )

        init?.invoke(builder)
        fieldDefBuilders.add(builder)

    }


    fun withPreAuthorize(expression: String): RequestDtoDefBuilder {

        this.preAuthorizeExpression = PreAuthorizeExpression(expression)
        return this

    }


}
