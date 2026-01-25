package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityDef
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
    private val idFieldName: String,
    private val sortByFieldName: String,
    private val searchTermFieldName: String,
    private val indexVersion: Int,
    private val withHandCodedEsDocRepo: WithHandCodedEsDocRepo
) {


    private val fieldBuilders = mutableListOf<TypeaheadFieldDefBuilder>()


    fun build(): TypeaheadDef {

        val fieldDefs = fieldBuilders.map { it.build() }

        `confirm that a field exists with the provided searchTermFieldName`(fieldDefs)

        return TypeaheadDef(
            this.packageName,
            this.typeaheadName,
            this.entityDef,
            ClassFieldName(this.idFieldName),
            this.sortByFieldName,
            this.searchTermFieldName,
            this.indexVersion,
            this.withHandCodedEsDocRepo,
            fieldDefs
        )

    }


    private fun `confirm that a field exists with the provided searchTermFieldName`(fieldDefs: List<TypeaheadFieldDef>) {

        require(fieldDefs.any { it.classFieldDef.classFieldName.value == this.searchTermFieldName }) { "No field name matches the searchTermFieldName '$searchTermFieldName'" }

    }


    fun fieldFromEntity(
        dtoFieldName: String,
        fieldType: FieldType? = null,
        entityFieldName: String? = null,
        esDocMappingType: EsDocMappingType? = null,
        init: (TypeaheadFieldDefBuilder.() -> Unit)? = null
    ) {

        val entityDefNonNull = this.entityDef ?: throw IllegalStateException("Cannot invoke fieldFromEntity without first providing an EntityDef")

        val entityFieldDef = entityDefNonNull.findFieldByName(entityFieldName ?: dtoFieldName)

        val indexMappingType = esDocMappingType
            ?: entityFieldDef.classFieldDef.fieldType.elasticMappingType
            ?: EsDocMappingTypes.text

        val fieldTypeToUse = fieldType ?: entityFieldDef.classFieldDef.fieldType

        val builder = TypeaheadFieldDefBuilder(dtoFieldName, fieldTypeToUse, entityFieldDef)
        builder.esDocMappingType = indexMappingType
        this.fieldBuilders.add(builder)
        init?.invoke(builder)

    }


    fun field(
        dtoFieldName: String,
        fieldType: FieldType,
        init: (TypeaheadFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = TypeaheadFieldDefBuilder(dtoFieldName, fieldType, entityFieldDef = null)
        this.fieldBuilders.add(builder)
        init?.invoke(builder)

    }


}
