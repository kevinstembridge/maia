package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.TypeaheadFieldDef
import org.maiaframework.gen.spec.definition.TypeaheadName
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEsDocRepo
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.PackageName


@MaiaDslMarker
class TypeaheadDefBuilder(
    private val packageName: PackageName,
    private val typeaheadName: TypeaheadName,
    private val entityDef: EntityDef?,
    private val sortByFieldName: String,
    private val searchTermFieldName: String,
    private val indexVersion: Int,
    private val withHandCodedEsDocRepo: WithHandCodedEsDocRepo
) {


    private val fieldDefs = mutableListOf<TypeaheadFieldDef>()


    fun build(): TypeaheadDef {

        `confirm exactly one ID field`()
        `confirm that a field exists with the provided searchTermFieldName`()

        return TypeaheadDef(
            this.packageName,
            this.typeaheadName,
            this.entityDef,
            this.sortByFieldName,
            this.searchTermFieldName,
            this.indexVersion,
            this.withHandCodedEsDocRepo,
            fieldDefs
        )

    }


    private fun `confirm exactly one ID field`() {

        require(this.fieldDefs.count { it.isIdField } == 1) { "There must be exactly one ID field on typeahead $typeaheadName. Found ${this.fieldDefs.filter { it.isIdField }}" }

    }


    private fun `confirm that a field exists with the provided searchTermFieldName`() {

        require(this.fieldDefs.any { it.classFieldDef.classFieldName.value == this.searchTermFieldName }) { "No field name matches the searchTermFieldName '$searchTermFieldName' on typeahead $typeaheadName" }

    }


    fun idField(
        dtoFieldName: String,
        fieldType: FieldType,
        init: (TypeaheadFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = TypeaheadFieldDefBuilder(dtoFieldName, fieldType, isIdField = true, entityFieldDef = null)
        init?.invoke(builder)
        this.fieldDefs.add(builder.build())

    }


    fun idFieldFromEntity(
        dtoFieldName: String,
        fieldType: FieldType? = null,
        entityFieldName: String? = null,
        esDocMappingType: EsDocMappingType? = null,
        init: (TypeaheadFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = fieldFromEntity(
            entityFieldName,
            dtoFieldName,
            esDocMappingType,
            fieldType,
            isIdField = true
        )

        init?.invoke(builder)
        this.fieldDefs.add(builder.build())

    }


    fun fieldFromEntity(
        dtoFieldName: String,
        fieldType: FieldType? = null,
        entityFieldName: String? = null,
        esDocMappingType: EsDocMappingType? = null,
        init: (TypeaheadFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = fieldFromEntity(
            entityFieldName,
            dtoFieldName,
            esDocMappingType,
            fieldType,
            isIdField = false
        )

        init?.invoke(builder)
        this.fieldDefs.add(builder.build())

    }


    private fun fieldFromEntity(
        entityFieldName: String?,
        dtoFieldName: String,
        esDocMappingType: EsDocMappingType?,
        fieldType: FieldType?,
        isIdField: Boolean
    ): TypeaheadFieldDefBuilder {

        val entityFieldDef = entityFieldDef(entityFieldName ?: dtoFieldName)

        val indexMappingType = esDocMappingType
            ?: entityFieldDef.classFieldDef.fieldType.elasticMappingType
            ?: EsDocMappingTypes.text

        val fieldTypeToUse = fieldType ?: entityFieldDef.classFieldDef.fieldType

        val builder = TypeaheadFieldDefBuilder(
            dtoFieldName,
            fieldTypeToUse,
            isIdField,
            entityFieldDef
        )

        builder.esDocMappingType = indexMappingType
        return builder

    }


    private fun entityFieldDef(fieldName: String): EntityFieldDef {

        val entityDefNonNull = this.entityDef
            ?: throw IllegalStateException("Cannot invoke fieldFromEntity without first providing an EntityDef. Typeahead name = ${this.typeaheadName}.")

        return entityDefNonNull.findFieldByName(fieldName)

    }


    fun field(
        dtoFieldName: String,
        fieldType: FieldType,
        init: (TypeaheadFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = TypeaheadFieldDefBuilder(dtoFieldName, fieldType, isIdField = false, entityFieldDef = null)
        init?.invoke(builder)
        this.fieldDefs.add(builder.build())

    }


}
