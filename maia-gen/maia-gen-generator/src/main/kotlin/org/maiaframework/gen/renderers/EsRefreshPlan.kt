package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.RefreshDependencyDef
import org.maiaframework.gen.spec.definition.RefreshRole
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType


/** The reverse joins of a hand-built EsDoc: one per [RefreshRole.PATH] source declared with `watchesVia`. */
internal object EsDocReverseJoins {


    fun reverseJoinsFor(esDocDef: EsDocDef): List<ReverseJoins.ReverseJoin> {

        return esDocDef.refreshDependencies
            .filter { it.role == RefreshRole.PATH }
            .map { ReverseJoins.ReverseJoin(it, it.entityDef.primaryKeyFields.single()) }

    }


}


/**
 * The EsDoc auto-refresh CrudListeners of a searchable DTO: one per (EsDoc version with `autoRefresh = true`) x
 * (the [RefreshRole.ROOT] dependency; each dependency that has a `findRootIdsVia<Entity>` reverse join per
 * [ReverseJoins], so a listener never calls a repo method that is not rendered; and each [RefreshRole.CHILD]
 * entity with a CrudListener whose foreign key to the root is a DomainId, read directly with no query).
 */
internal object EsRefreshListeners {


    fun renderersFor(searchableDtoDef: SearchableDtoDef): List<SearchableDtoEsDocCrudListenerRenderer> {

        val autoRefreshVersions = searchableDtoDef.esVersions.filter { it.autoRefresh }.map { it.version }.toSet()

        // refreshDependencies validates on first read, so it must only be read when auto-refresh is on.
        if (autoRefreshVersions.isEmpty()) {
            return emptyList()
        }

        val dependencies = listenedDependencies(searchableDtoDef)

        return searchableDtoDef.esDocDefs
            .filter { it.esDocVersion in autoRefreshVersions }
            .flatMap { esDocDef -> dependencies.map { SearchableDtoEsDocCrudListenerRenderer(searchableDtoDef, esDocDef, it) } }

    }


    /**
     * The dependencies of an auto-refreshed searchable DTO that get a listener, whatever the store: the ROOT; each
     * dependency with a `findRootIdsVia<Entity>` reverse join; each CHILD with a CrudListener and a DomainId foreign key.
     * Only call when the searchable DTO has auto-refresh on.
     */
    fun listenedDependencies(searchableDtoDef: SearchableDtoDef): List<RefreshDependencyDef> {

        val reverseJoinDependencies = ReverseJoins.reverseJoinsFor(searchableDtoDef).map { it.dependency }

        return searchableDtoDef.refreshDependencies.filter { dependency ->
            when (dependency.role) {
                RefreshRole.ROOT -> true
                RefreshRole.CHILD -> dependency.hasCrudListener && rootForeignKeyFieldOf(dependency) != null
                RefreshRole.PATH, RefreshRole.OTHER_SIDE -> reverseJoinDependencies.any { it === dependency }
            }
        }

    }


    /**
     * The listeners of a hand-built EsDoc declared with `autoRefresh(true)`: one per declared source entity. Same-row
     * (ROOT) and foreign-key child (CHILD) sources need no query; `watchesVia` (PATH) sources call the
     * `findRootIdsVia<Entity>` queries of the [EsDocRefreshRepoRenderer]'s repo. Empty for any other EsDoc.
     */
    fun renderersFor(esDocDef: EsDocDef): List<SearchableDtoEsDocCrudListenerRenderer> {

        return esDocDef.refreshDependencies.map { SearchableDtoEsDocCrudListenerRenderer(null, esDocDef, it) }

    }


    /**
     * The field of a many-to-many join entity that holds the root id: the target field of the single path's first
     * hop (root primary key -> join entity foreign key). Null, so that no listener is rendered, unless that is a
     * foreign key to a single DomainId primary key. The root rule (a single DomainId `id`) makes null unreachable
     * through the DSL today.
     */
    fun rootForeignKeyFieldOf(dependency: RefreshDependencyDef): EntityFieldDef? {

        val rootForeignKeyField = dependency.pathsFromRoot.singleOrNull()?.firstOrNull()?.toField ?: return null
        val foreignKeyFieldType = rootForeignKeyField.fieldType as? ForeignKeyFieldType ?: return null
        val referencedPrimaryKey = foreignKeyFieldType.foreignKeyFieldDef.foreignEntityDef.primaryKeyFields.singleOrNull() ?: return null

        return if (referencedPrimaryKey.classFieldDef.fieldType is DomainIdFieldType) rootForeignKeyField else null

    }


}


/**
 * The Hazelcast auto-refresh CrudListeners of a Hazelcast-backed searchable DTO with `autoRefresh = true`: one per
 * dependency, chosen as for the EsDoc listeners (see [EsRefreshListeners.listenedDependencies]).
 */
internal object HazelcastRefreshListeners {


    fun renderersFor(searchableDtoDef: SearchableDtoDef): List<SearchableDtoEsDocCrudListenerRenderer> {

        // refreshDependencies validates on first read, so it must only be read when auto-refresh is on.
        if (searchableDtoDef.hazelcastSpec?.autoRefresh != true) {
            return emptyList()
        }

        return EsRefreshListeners.listenedDependencies(searchableDtoDef)
            .map { SearchableDtoEsDocCrudListenerRenderer.forHazelcast(searchableDtoDef, it) }

    }


}
