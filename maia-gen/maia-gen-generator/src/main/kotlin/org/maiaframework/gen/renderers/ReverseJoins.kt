package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.RefreshDependencyDef
import org.maiaframework.gen.spec.definition.RefreshRole
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType


/**
 * The `findRootIdsVia<Entity>` reverse joins of a document-backed searchable DTO: one per [RefreshRole.PATH] or
 * [RefreshRole.OTHER_SIDE] dependency that has a CrudListener and a single DomainId primary key field. A
 * [RefreshRole.CHILD] entity gets none: its auto-refresh listener reads the root id straight from its foreign
 * key to the root. Shared by the DTO DAO and repo renderers and the document auto-refresh CrudListener renderers, so
 * that all of them agree on exactly the same set of functions.
 */
internal object ReverseJoins {


    class ReverseJoin(
        val dependency: RefreshDependencyDef,
        val primaryKeyField: EntityFieldDef
    ) {


        val functionName = "findRootIdsVia${dependency.entityDef.entityBaseName}"


    }


    fun reverseJoinsFor(searchableDtoDef: SearchableDtoDef): List<ReverseJoin> {

        // refreshDependencies validates on first read, so it must only be read when auto-refresh is on.
        if (!searchableDtoDef.hasAutoRefresh) {
            return emptyList()
        }

        return searchableDtoDef.refreshDependencies
            .filter { (it.role == RefreshRole.PATH || it.role == RefreshRole.OTHER_SIDE) && it.hasCrudListener }
            .mapNotNull { dependency ->
                // Kept, as is the hasCrudListener filter: the factory validates every entity that is not a many-to-many
                // join entity, which does not provably cover an OTHER_SIDE entity that is also a join entity elsewhere.
                dependency.entityDef.primaryKeyFields.singleOrNull()
                    ?.takeIf { it.classFieldDef.fieldType is DomainIdFieldType }
                    ?.let { ReverseJoin(dependency, it) }
            }

    }


}
