package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.lang.text.StringFunctions


object SearchableDtoEsDocFactory {


    fun build(
        packageName: PackageName,
        dtoBaseName: DtoBaseName,
        fields: List<SearchableDtoFieldDef>,
        idFieldName: ClassFieldName?,
        entityDef: EntityDef,
        spec: EsVersionSpec
    ): EsDocDef {

        val esDocFields = fields.map { field ->

            val isIdField = field.classFieldName == idFieldName

            when (field) {

                is SimpleSearchableDtoFieldDef -> EsDocFieldDef(
                    field.classFieldDef,
                    field.esMappingOverride ?: defaultMapping(field.classFieldDef),
                    field.entityFieldDef,
                    isIdField
                )

                is ManyToManySearchableDtoFieldDef -> EsDocFieldDef(
                    field.classFieldDef,
                    EsDocMappingTypes.`object`,
                    null,
                    isIdField
                )

                // defensive: elasticsearchEnabled() already rejects composite-PK root entities
                is CompositeIdFieldDef -> throw IllegalArgumentException("elasticsearchEnabled requires a single-column id (composite id unsupported)")

            }

        }

        return EsDocDef(
            packageName,
            dtoBaseName,
            ElasticIndexBaseName(StringFunctions.toSnakeCase(dtoBaseName.value)),
            spec.version,
            Description("Index for ${dtoBaseName.value}"),
            esDocFields,
            renderFieldEnum = false,
            // deliberately independent of autoRefresh: the scheduled safety-net refresh job is always generated
            generateRefreshIndexJob = true,
            disableRendering = false,
            entityDef
        )

    }


    private fun defaultMapping(classFieldDef: ClassFieldDef): EsDocMappingType {

        val fieldType = classFieldDef.fieldType

        return when (fieldType) {

            // blotters must sort and filter on strings, so use text plus a keyword sub-field
            is StringFieldType, is StringTypeFieldType, is StringValueClassFieldType -> EsDocMappingTypes.textAndKeyword

            // FieldType declares Instant as text, but derived blotter-backed docs need date (range filtering and sorting)
            is InstantFieldType -> EsDocMappingTypes.date

            else -> fieldType.elasticMappingType ?: throw IllegalArgumentException(
                "No default ES mapping for field ${classFieldDef.classFieldName.value} (${fieldType::class.simpleName}); declare esMapping(...)"
            )

        }

    }


}
