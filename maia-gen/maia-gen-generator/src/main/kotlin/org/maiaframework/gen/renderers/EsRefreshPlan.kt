package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EsRefreshDependencyDef
import org.maiaframework.gen.spec.definition.EsRefreshRole
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType


/**
 * The `findRootIdsVia<Entity>` reverse joins of an ES-enabled searchable DTO: one per [EsRefreshRole.PATH] or
 * [EsRefreshRole.OTHER_SIDE] dependency that has a CrudListener and a single DomainId primary key field. A
 * [EsRefreshRole.JOIN] entity gets none: its auto-refresh listener reads the root id straight from its foreign
 * key to the root. Shared by the DTO DAO and repo renderers and the EsDoc auto-refresh CrudListener renderer, so
 * that all of them agree on exactly the same set of functions.
 */
internal object EsReverseJoins {


    class ReverseJoin(
        val dependency: EsRefreshDependencyDef,
        val primaryKeyField: EntityFieldDef
    ) {


        val functionName = "findRootIdsVia${dependency.entityDef.entityBaseName}"


    }


    fun reverseJoinsFor(searchableDtoDef: SearchableDtoDef): List<ReverseJoin> {

        // esRefreshDependencies validates on first read, so it must only be read when auto-refresh is on.
        if (searchableDtoDef.esVersions.none { it.autoRefresh }) {
            return emptyList()
        }

        return searchableDtoDef.esRefreshDependencies
            .filter { (it.role == EsRefreshRole.PATH || it.role == EsRefreshRole.OTHER_SIDE) && it.hasCrudListener }
            .mapNotNull { dependency ->
                // Kept, as is the hasCrudListener filter: the factory validates every entity that is not a many-to-many
                // join entity, which does not provably cover an OTHER_SIDE entity that is also a join entity elsewhere.
                dependency.entityDef.primaryKeyFields.singleOrNull()
                    ?.takeIf { it.classFieldDef.fieldType is DomainIdFieldType }
                    ?.let { ReverseJoin(dependency, it) }
            }

    }


}


/**
 * The EsDoc auto-refresh CrudListeners of a searchable DTO: one per (EsDoc version with `autoRefresh = true`) x
 * (the [EsRefreshRole.ROOT] dependency; each dependency that has a `findRootIdsVia<Entity>` reverse join per
 * [EsReverseJoins], so a listener never calls a repo method that is not rendered; and each [EsRefreshRole.JOIN]
 * entity with a CrudListener whose foreign key to the root is a DomainId, read directly with no query).
 */
internal object EsRefreshListeners {


    fun renderersFor(searchableDtoDef: SearchableDtoDef): List<SearchableDtoEsDocCrudListenerRenderer> {

        val autoRefreshVersions = searchableDtoDef.esVersions.filter { it.autoRefresh }.map { it.version }.toSet()

        // esRefreshDependencies validates on first read, so it must only be read when auto-refresh is on.
        if (autoRefreshVersions.isEmpty()) {
            return emptyList()
        }

        val reverseJoinDependencies = EsReverseJoins.reverseJoinsFor(searchableDtoDef).map { it.dependency }

        val dependencies = searchableDtoDef.esRefreshDependencies.filter { dependency ->
            when (dependency.role) {
                EsRefreshRole.ROOT -> true
                EsRefreshRole.JOIN -> dependency.hasCrudListener && rootForeignKeyFieldOf(dependency) != null
                EsRefreshRole.PATH, EsRefreshRole.OTHER_SIDE -> reverseJoinDependencies.any { it === dependency }
            }
        }

        return searchableDtoDef.esDocDefs
            .filter { it.esDocVersion in autoRefreshVersions }
            .flatMap { esDocDef -> dependencies.map { SearchableDtoEsDocCrudListenerRenderer(searchableDtoDef, esDocDef, it) } }

    }


    /**
     * The field of a many-to-many join entity that holds the root id: the target field of the single path's first
     * hop (root primary key -> join entity foreign key). Null, so that no listener is rendered, unless that is a
     * foreign key to a single DomainId primary key. The root rule (a single DomainId `id`) makes null unreachable
     * through the DSL today.
     */
    fun rootForeignKeyFieldOf(dependency: EsRefreshDependencyDef): EntityFieldDef? {

        val rootForeignKeyField = dependency.pathsFromRoot.singleOrNull()?.firstOrNull()?.toField ?: return null
        val foreignKeyFieldType = rootForeignKeyField.fieldType as? ForeignKeyFieldType ?: return null
        val referencedPrimaryKey = foreignKeyFieldType.foreignKeyFieldDef.foreignEntityDef.primaryKeyFields.singleOrNull() ?: return null

        return if (referencedPrimaryKey.classFieldDef.fieldType is DomainIdFieldType) rootForeignKeyField else null

    }


}
