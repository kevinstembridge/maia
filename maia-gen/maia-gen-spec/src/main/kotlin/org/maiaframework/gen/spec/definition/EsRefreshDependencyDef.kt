package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType


/**
 * One join step: `fromEntity.fromField = toEntity.toField`.
 *
 * Equality is by entity base names and field names only, so path de-duplication does not depend on
 * structural hashing of the underlying field definitions.
 */
class JoinHop(
    val fromEntity: EntityDef,
    /** A column/field on [fromEntity]. */
    val fromField: EntityFieldDef,
    val toEntity: EntityDef,
    /** A column/field on [toEntity]. */
    val toField: EntityFieldDef
) {


    private val key = listOf(
        fromEntity.entityBaseName.value,
        fromField.classFieldName.value,
        toEntity.entityBaseName.value,
        toField.classFieldName.value
    )


    override fun equals(other: Any?): Boolean {

        return other is JoinHop && other.key == this.key

    }


    override fun hashCode(): Int {

        return key.hashCode()

    }


    override fun toString(): String {

        return "${fromEntity.entityBaseName}.${fromField.classFieldName}->${toEntity.entityBaseName}.${toField.classFieldName}"

    }


}


/**
 * How an [EsRefreshDependencyDef]'s changes map to the root ids whose EsDocs must be refreshed.
 */
enum class EsRefreshRole {

    /** The searchable DTO's root entity: a changed row refreshes its own id. */
    ROOT,

    /** Reached from the root through foreign keys: affected root ids are resolved by a reverse query. */
    PATH,

    /** The join entity of a many-to-many: its foreign key to the root is the affected root id. */
    JOIN,

    /** The other side of a many-to-many: affected root ids are resolved twice (before the change and after commit). */
    OTHER_SIDE;


    companion object {


        fun of(isRoot: Boolean, isManyToManyMembership: Boolean, refreshOnAnyUpdate: Boolean): EsRefreshRole {

            return when {
                isRoot -> ROOT
                isManyToManyMembership && refreshOnAnyUpdate -> OTHER_SIDE
                isManyToManyMembership -> JOIN
                else -> PATH
            }

        }


    }

}


/**
 * An entity whose changes can alter the EsDocs derived from a searchable DTO.
 *
 * [entityDef] is the entity that the DTO's field path (or many-to-many join) actually resolves to.
 * When a field is declared on a superclass of that entity, the dependency is still recorded against
 * the path entity: its CrudNotifier delivers events to its own CrudListener (and, recursively, to
 * the listeners of its superclasses), so a listener attached to the path entity sees every change
 * to the rows the DTO joins. If the path entity is itself abstract (a foreign key to an abstract
 * superclass), its CrudListener only receives events from concrete subclasses that also have
 * `withCrudListener` enabled; that cannot be verified here because an EntityDef does not know its
 * subclasses.
 *
 * Consumer notes: (i) SQL generated from [pathsFromRoot] must alias each joined table per hop index,
 * because the same entity can appear more than once in a path; (ii) subclass entities share their
 * superclass's table, so a hop INTO a subclass entity whose table has a type discriminator needs the
 * discriminator predicate in the generated SQL; (iii) superclass listeners receive subclass events via
 * the CrudNotifier's superclass recursion, but events for a concrete SUPERCLASS row updated through
 * a subclass's service are not guaranteed to reach a listener registered only on the other class.
 */
class EsRefreshDependencyDef(
    val entityDef: EntityDef,
    val isRoot: Boolean,
    /** Entity class field names whose change can alter the EsDoc. */
    val watchedFields: Set<ClassFieldName>,
    /**
     * Each path is ordered root to [entityDef]. The root's own dependency contains the empty path,
     * and may also contain non-empty paths when the root is reached again through a foreign key.
     */
    val pathsFromRoot: List<List<JoinHop>>,
    /**
     * True when changes cannot be detected by diffing [watchedFields]: set on the root and on the
     * other-side entity of a many-to-many join (whether reached through a many-to-many field or a
     * join-qualified field path). The join entity itself is NOT flagged. The generated Left/Right
     * CrudServices reconcile join rows directly through the join entity's repo AFTER `setFields` has
     * fired `onEntityUpdated`, and no join-entity CrudNotifier is called, so an update event on either
     * side may coincide with an unseen membership change. Listeners must therefore resolve affected root ids twice: at event
     * time (pre-reconcile membership) and after commit (post-reconcile membership).
     */
    val refreshOnAnyUpdate: Boolean,
    /**
     * True for the join entity or other-side entity of a many-to-many join. Root ids reached through
     * the other-side entity are subject to the two-phase resolution described on [refreshOnAnyUpdate].
     * The join entity (membership without [refreshOnAnyUpdate]) needs no resolution query: its foreign
     * key to the root (the target field of its single path's first hop) is the root id itself.
     */
    val isManyToManyMembership: Boolean,
    /** Derived from [isRoot], [isManyToManyMembership] and [refreshOnAnyUpdate] by [EsRefreshRole.of]. */
    val role: EsRefreshRole
) {


    /**
     * Whether a CrudListener exists for [entityDef]. Always true except possibly for a many-to-many
     * join entity, which is exempt from validation (e.g. a `simpleManyToManyEntity` cannot declare
     * `crud {}`, and join-row edits made by the Left/Right CrudServices never notify it anyway).
     */
    val hasCrudListener: Boolean = entityDef.crudDef.withCrudListener.value


}


object EsRefreshDependencyFactory {


    private val auditFieldNames = setOf(
        ClassFieldName.createdById,
        ClassFieldName.createdByUsername,
        ClassFieldName.createdTimestamp,
        ClassFieldName.lastModifiedById,
        ClassFieldName.lastModifiedByUsername,
        ClassFieldName.lastModifiedTimestamp,
        ClassFieldName.version
    )


    fun build(
        rootEntityDef: EntityDef,
        dtoBaseName: DtoBaseName,
        fields: List<SearchableDtoFieldDef>,
        manyToManyJoinEntityDefs: List<JoinEntityDef>
    ): List<EsRefreshDependencyDef> {

        val collector = Collector(rootEntityDef, dtoBaseName, manyToManyJoinEntityDefs)

        fields.forEach { field ->
            when (field) {
                is SimpleSearchableDtoFieldDef -> collector.addSimpleField(field)
                is ManyToManySearchableDtoFieldDef -> collector.addManyToManyField(field)
                is CompositeIdFieldDef -> Unit
            }
        }

        manyToManyJoinEntityDefs.forEach { collector.addJoinEntity(it.entityDef) }

        val dependencies = collector.build()

        // Defensive: the DSL cannot currently build this (an entity can only reference entities defined before it,
        // so no foreign-key path can lead back to the root), hence no test. The root listener refreshes only the
        // changed row's own id, so documents of other root rows that reach it through such a path would go stale.
        dependencies.filter { it.isRoot }.forEach { root ->
            require(root.pathsFromRoot.all { it.isEmpty() }) {
                "Elasticsearch auto-refresh on searchable DTO $dtoBaseName: a path that leads back to the root entity ${root.entityDef.entityBaseName} " +
                    "is not supported for automatic refresh: documents of other rows that reference a changed row would go stale. Paths: ${root.pathsFromRoot}"
            }
        }

        dependencies.filterNot { collector.isJoin(it.entityDef) }.forEach { dependency ->
            require(dependency.hasCrudListener) {
                "Elasticsearch auto-refresh on searchable DTO $dtoBaseName requires entity ${dependency.entityDef.entityBaseName} " +
                    "to have a CrudListener (declare a crud {} block on it). It contributes to the EsDoc via paths ${dependency.pathsFromRoot}"
            }
            // The generated listeners and reverse joins pass the entity's primary key as a DomainId.
            require(dependency.entityDef.primaryKeyFields.singleOrNull()?.classFieldDef?.fieldType is DomainIdFieldType) {
                "Elasticsearch auto-refresh on searchable DTO $dtoBaseName: entity ${dependency.entityDef.entityBaseName} " +
                    "requires a single DomainId primary key; found ${dependency.entityDef.primaryKeyFields.map { "${it.classFieldName}: ${it.classFieldDef.fieldType}" }}"
            }
        }

        return dependencies

    }


    private class DependencyBuilder(
        val entityDef: EntityDef,
        val isRoot: Boolean
    ) {


        val watchedFields = linkedSetOf<ClassFieldName>()


        val paths = linkedSetOf<List<JoinHop>>()


        var refreshOnAnyUpdate = false


        var isManyToManyMembership = false


        var isJoin = false


        fun build(): EsRefreshDependencyDef {

            return EsRefreshDependencyDef(
                entityDef,
                isRoot,
                watchedFields.toSet(),
                paths.toList(),
                refreshOnAnyUpdate,
                isManyToManyMembership,
                EsRefreshRole.of(isRoot, isManyToManyMembership, refreshOnAnyUpdate)
            )

        }


    }


    private class Collector(
        private val rootEntityDef: EntityDef,
        private val dtoBaseName: DtoBaseName,
        private val manyToManyJoinEntityDefs: List<JoinEntityDef>
    ) {


        private val builders = linkedMapOf<EntityDef, DependencyBuilder>()


        init {

            val rootBuilder = DependencyBuilder(rootEntityDef, isRoot = true)
            rootBuilder.paths.add(emptyList())
            rootBuilder.refreshOnAnyUpdate = manyToManyJoinEntityDefs.isNotEmpty()
            builders[rootEntityDef] = rootBuilder

        }


        fun addSimpleField(field: SimpleSearchableDtoFieldDef) {

            val fieldPath = field.fieldPath
            val head = fieldPath.head()

            if (rootEntityDef.findFieldByNameOrNull(head) != null) {

                walk(rootEntityDef, fieldPath, emptyList())

            } else {

                val joinEntityDef = manyToManyJoinEntityDefs.firstOrNull { it.entityDef.entityBaseName.value == head }?.entityDef
                    ?: throw IllegalArgumentException("Cannot resolve field path '$fieldPath' from root entity ${rootEntityDef.entityBaseName} on searchable DTO $dtoBaseName")

                val joinPath = addJoinEntity(joinEntityDef)
                val tail = fieldPath.tail()

                walk(joinEntityDef, tail, joinPath)

                if (tail.isJustOneField() == false) {
                    markOtherSideReachedThroughJoin(joinEntityDef, joinEntityDef.findFieldByName(tail.head()), joinPath.single().toField)
                }

            }

        }


        /**
         * A join-qualified path (`J.otherFk.x`) reaches the other side of the many-to-many through
         * the join entity's non-root foreign key: treat that entity exactly like the other side of a
         * many-to-many field.
         */
        private fun markOtherSideReachedThroughJoin(
            joinEntityDef: EntityDef,
            joinFkField: EntityFieldDef,
            rootFkField: EntityFieldDef
        ) {

            if (joinFkField.classFieldName == rootFkField.classFieldName) {
                return
            }

            val otherEntityDef = (joinFkField.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef
            val otherBuilder = builderFor(otherEntityDef)
            otherBuilder.isManyToManyMembership = true
            otherBuilder.refreshOnAnyUpdate = true

        }


        fun addManyToManyField(field: ManyToManySearchableDtoFieldDef) {

            val manyToManyEntityDef = field.manyToManyEntityDef
            val joinEntityDef = manyToManyEntityDef.entityDef

            requireNotSelfManyToMany(manyToManyEntityDef.leftEntity.entityDef == manyToManyEntityDef.rightEntity.entityDef, joinEntityDef)

            val rootFkField = joinEntityDef.findFieldByName(manyToManyEntityDef.idFieldName(rootEntityDef))
            val joinPath = addJoinEntity(joinEntityDef, rootFkField)

            val otherSide = manyToManyEntityDef.otherSideFrom(rootEntityDef)
            val otherEntityDef = otherSide.entityDef
            val otherFkField = joinEntityDef.findFieldByName(otherSide.fieldName)

            val otherBuilder = builderFor(otherEntityDef)
            otherBuilder.isManyToManyMembership = true
            otherBuilder.refreshOnAnyUpdate = true
            otherBuilder.paths.add(joinPath + JoinHop(joinEntityDef, otherFkField, otherEntityDef, primaryKeyFieldOf(otherEntityDef)))

            val pkAndNameDef = otherEntityDef.entityPkAndNameDef
            otherBuilder.watchedFields.add(pkAndNameDef.nameEntityFieldDef.classFieldName)
            pkAndNameDef.shortcodeEntityFieldDef?.let { otherBuilder.watchedFields.add(it.classFieldName) }

        }


        /**
         * Registers [joinEntityDef] as a many-to-many membership dependency and returns its path from the root.
         */
        fun addJoinEntity(
            joinEntityDef: EntityDef,
            providedRootFkField: EntityFieldDef? = null
        ): List<JoinHop> {

            val rootFkField = providedRootFkField ?: findRootForeignKeyOn(joinEntityDef)
            val joinPath = listOf(JoinHop(rootEntityDef, primaryKeyFieldOf(rootEntityDef), joinEntityDef, rootFkField))

            val joinBuilder = builderFor(joinEntityDef)
            joinBuilder.isManyToManyMembership = true
            joinBuilder.isJoin = true
            joinBuilder.paths.add(joinPath)

            joinEntityDef.allEntityFieldsSorted
                .filterNot { it.isPrimaryKey.value }
                .filterNot { it.classFieldName in auditFieldNames }
                .forEach { joinBuilder.watchedFields.add(it.classFieldName) }

            return joinPath

        }


        private fun findRootForeignKeyOn(joinEntityDef: EntityDef): EntityFieldDef {

            val rootFkFields = joinEntityDef.allEntityFieldsSorted.filter { field ->
                (field.fieldType as? ForeignKeyFieldType)?.foreignKeyFieldDef?.foreignEntityDef == rootEntityDef
            }

            require(rootFkFields.size <= 1) {
                "Elasticsearch auto-refresh: join entity ${joinEntityDef.entityBaseName} has more than one foreign key to the root entity ${rootEntityDef.entityBaseName} " +
                    "(${rootFkFields.map { it.classFieldName }}); cannot choose a join path on searchable DTO $dtoBaseName"
            }

            return rootFkFields.singleOrNull()
                ?: throw IllegalArgumentException("Join entity ${joinEntityDef.entityBaseName} on searchable DTO $dtoBaseName has no foreign key to the root entity ${rootEntityDef.entityBaseName}")

        }


        private fun requireNotSelfManyToMany(isSelfManyToMany: Boolean, joinEntityDef: EntityDef) {

            require(isSelfManyToMany == false) {
                "Elasticsearch auto-refresh: self many-to-many not supported (join entity ${joinEntityDef.entityBaseName} joins ${rootEntityDef.entityBaseName} to itself) on searchable DTO $dtoBaseName"
            }

        }


        private fun walk(
            entityDef: EntityDef,
            fieldPath: FieldPath,
            hopsSoFar: List<JoinHop>
        ) {

            val builder = builderFor(entityDef)
            builder.paths.add(hopsSoFar)

            val entityFieldDef = entityDef.findFieldByName(fieldPath.head())

            if (fieldPath.isJustOneField()) {

                if (entityFieldDef.isPrimaryKey.value == false) {
                    builder.watchedFields.add(entityFieldDef.classFieldName)
                }

                return

            }

            val fieldType = entityFieldDef.fieldType as? ForeignKeyFieldType
                ?: throw IllegalArgumentException("Field ${entityFieldDef.classFieldName} on entity ${entityDef.entityBaseName} is not a foreign key; path '$fieldPath' on searchable DTO $dtoBaseName")

            val foreignEntityDef = fieldType.foreignKeyFieldDef.foreignEntityDef

            builder.watchedFields.add(entityFieldDef.classFieldName)

            val hop = JoinHop(entityDef, entityFieldDef, foreignEntityDef, primaryKeyFieldOf(foreignEntityDef))

            walk(foreignEntityDef, fieldPath.tail(), hopsSoFar + hop)

        }


        private fun primaryKeyFieldOf(entityDef: EntityDef): EntityFieldDef {

            return entityDef.primaryKeyFields.singleOrNull()
                ?: throw IllegalArgumentException("Elasticsearch auto-refresh on searchable DTO $dtoBaseName requires entity ${entityDef.entityBaseName} to have a single primary key field; found ${entityDef.primaryKeyFields.map { it.classFieldName }}")

        }


        fun isJoin(entityDef: EntityDef): Boolean {

            return builders[entityDef]?.isJoin == true

        }


        private fun builderFor(entityDef: EntityDef): DependencyBuilder {

            return builders.getOrPut(entityDef) { DependencyBuilder(entityDef, isRoot = false) }

        }


        /**
         * Drops non-root, non-membership entities that contribute no watched field (e.g. a path
         * that only reads the foreign entity's primary key, which never changes).
         */
        fun build(): List<EsRefreshDependencyDef> {

            return builders.values
                .filter { it.isRoot || it.isManyToManyMembership || it.watchedFields.isNotEmpty() }
                .map { it.build() }

        }


    }


}
