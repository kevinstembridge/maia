package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.domain.persist.SortDirection
import java.util.concurrent.TimeUnit


@MaiaDslMarker
class IndexDefBuilder(
    private val entityBaseName: EntityBaseName
) {

    private val sortDirectionByFieldName = LinkedHashMap<String, SortDirection>()
    private var indexName: IndexName? = null
    private var unique: Boolean = false
    private var sparse: Boolean = false
    private var expireAfterSeconds: Long? = null
    private var withExistsEndpoint: Boolean = false
    private var withPreAuthorize: WithPreAuthorize? = null


    fun indexName(indexName: String) {

        this.indexName = IndexName(indexName)

    }


    fun withFieldAscending(fieldName: String): IndexDefBuilder {

        this.sortDirectionByFieldName[fieldName] = SortDirection.asc
        return this

    }


    fun withFieldDescending(fieldName: String): IndexDefBuilder {

        this.sortDirectionByFieldName[fieldName] = SortDirection.desc
        return this

    }


    fun unique(): IndexDefBuilder {

        this.unique = true
        return this

    }


    fun sparse(): IndexDefBuilder {

        this.sparse = true
        return this

    }


    fun build(entityFieldDefs: List<EntityFieldDef>): IndexDef {

        val indexFieldDefs = entityFieldDefs
                .asSequence()
                .filter { entityFieldDef -> this.sortDirectionByFieldName.containsKey(entityFieldDef.classFieldDef.classFieldName.value) }
                .map { entityFieldDef ->
                    IndexFieldDef(
                        entityFieldDef,
                        this.sortDirectionByFieldName[entityFieldDef.classFieldDef.classFieldName.value]!!
                    )
                }.toList()

        val resolvedFieldNames = indexFieldDefs.map { it.entityFieldDef.classFieldDef.classFieldName.value }.toSet()
        val unresolvedFieldNames = sortDirectionByFieldName.keys - resolvedFieldNames
        if (unresolvedFieldNames.isNotEmpty()) {
            throw IllegalStateException("An index definition for entity '$entityBaseName' declares fields named $unresolvedFieldNames that do not exist on the entity. The entity's fields are named ${entityFieldDefs.map { it.classFieldName }}.")
        }

        return IndexDef(
            this.entityBaseName,
            this.indexName,
            indexFieldDefs,
            this.unique,
            this.sparse,
            this.expireAfterSeconds,
            this.withExistsEndpoint,
            this.withPreAuthorize
        )

    }


    fun withExpireAfter(duration: Int, timeUnit: TimeUnit): IndexDefBuilder {

        this.expireAfterSeconds = timeUnit.toSeconds(duration.toLong())
        return this

    }


    fun withExistsEndpoint(): IndexDefBuilder {

        this.withExistsEndpoint = true
        return this

    }


}
