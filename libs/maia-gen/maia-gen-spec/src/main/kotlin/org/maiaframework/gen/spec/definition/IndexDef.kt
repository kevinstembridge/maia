package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName


class IndexDef(
    private val entityBaseName: EntityBaseName,
    indexName: IndexName? = null,
    val indexFieldDefs: List<IndexFieldDef>,
    val isUnique: Boolean,
    val isSparse: Boolean,
    val expireAfterSeconds: Long? = null,
    val withExistsEndpoint: Boolean = false,
    val withPreAuthorize: WithPreAuthorize? = null
) {


    val entityFieldDefs: List<EntityFieldDef> = this.indexFieldDefs.map { it.entityFieldDef }


    val fieldCount: Int = this.indexFieldDefs.size


    val isMultiField = fieldCount > 1


    val classFieldDefs: List<ClassFieldDef> = entityFieldDefs.map { it.classFieldDef }


    val indexName = indexName ?: createIndexName()


    val isForIdAndVersion = setOf(ClassFieldName.id, ClassFieldName.version) == indexFieldDefs.map { it.entityFieldDef.classFieldName }.toSet()


    private fun createIndexName(): IndexName {

        val uniquePrefix = if (isUnique) "u" else ""
        val fieldNames = indexFieldDefs.joinToString("_") { fd -> fd.databaseColumnName.toValidJavaIdentifier() }

        return IndexName("${entityBaseName.toSnakeCase()}_${fieldNames}_${uniquePrefix}idx")

    }


    fun asNonUnique(): IndexDef {

        return if (isUnique) {
            IndexDef(
                entityBaseName,
                indexName.replaceUidxSuffix(),
                indexFieldDefs,
                isUnique = false,
                isSparse,
                expireAfterSeconds,
                withExistsEndpoint,
                withPreAuthorize
            )
        } else {
            this
        }

    }


    fun withNamePrefix(prefix: String): IndexDef {

        return IndexDef(
            entityBaseName,
            IndexName("${prefix}_${indexName.value}"),
            indexFieldDefs,
            isUnique,
            isSparse,
            expireAfterSeconds,
            withExistsEndpoint,
            withPreAuthorize
        )

    }


}
