package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.PackageName


@MaiaDslMarker
class EsDocDefBuilder(
    private val packageName: PackageName,
    private val esDocBaseName: DtoBaseName,
    private val elasticIndexBaseName: ElasticIndexBaseName,
    private val esDocVersion: Int,
    private val indexDescription: Description,
    private val renderFieldEnum: Boolean,
    private val generateRefreshIndexJob: Boolean,
    private val disableRendering: Boolean
) {

    private val fieldDefs = mutableListOf<EsDocFieldDef>()


    private val auditFieldNames = setOf(
        ClassFieldName.createdById,
        ClassFieldName.createdByUsername,
        ClassFieldName.createdTimestamp,
        ClassFieldName.lastModifiedById,
        ClassFieldName.lastModifiedByUsername,
        ClassFieldName.lastModifiedTimestamp,
        ClassFieldName.version
    )

    /** `null` means the spec author has not declared a refresh strategy. */
    var autoRefresh: Boolean? = null
        private set


    private var sourceEntityDef: EntityDef? = null


    private var sourceEntityWatchedFields: List<String> = emptyList()


    private class SubclassSource(val entityDef: EntityDef, val watchedFields: List<String>)


    private class ChildSource(val entityDef: EntityDef, val rootForeignKey: String, val watchedFields: List<String>)


    private class ViaSource(val entityDef: EntityDef, val steps: ViaPathBuilder.() -> Unit, val description: String, val watchedFields: List<String>)


    private val subclassSources = mutableListOf<SubclassSource>()


    private val viaSources = mutableListOf<ViaSource>()


    private val childSources = mutableListOf<ChildSource>()


    /**
     * Declares how this hand-built EsDoc's index is refreshed. `false` means the spec author refreshes the index
     * themselves (a hand-written index service and/or the scheduled refresh job). `true` asks the generator to
     * refresh it automatically when its source rows change, which needs the sources declared with [sourceEntity],
     * [watchesSubclass] and [watchesChild]. The hand-written `<EsDoc>V<n>IndexService` must then implement
     * `EsRefreshTarget`.
     */
    fun autoRefresh(enabled: Boolean) {

        this.autoRefresh = enabled

    }


    /**
     * The entity whose id is the EsDoc's id. A change to one of [watchedFields] on it (or to any field, when none are
     * given), and its creation or deletion, refreshes that document.
     */
    fun sourceEntity(entityDef: EntityDef, vararg watchedFields: String) {

        check(this.sourceEntityDef == null) { "sourceEntity() can only be declared once" }
        this.sourceEntityDef = entityDef
        this.sourceEntityWatchedFields = watchedFields.toList()

    }


    /**
     * A subclass of the [sourceEntity] that stores part of the same logical row, so its id is the EsDoc's id. A
     * change to one of [watchedFields] on it (or to any field, when none are given) refreshes that document.
     */
    fun watchesSubclass(entityDef: EntityDef, vararg watchedFields: String) {

        this.subclassSources.add(SubclassSource(entityDef, watchedFields.toList()))

    }


    /**
     * An entity with many rows per [sourceEntity] row, linked by the foreign key [rootForeignKey] that holds the
     * EsDoc's id. Creating or deleting a row, or changing one of [watchedFields] (by default every field except the
     * primary key and audit fields), refreshes the document of the row it references, before and after the change.
     */
    fun watchesChild(entityDef: EntityDef, rootForeignKey: String, vararg watchedFields: String) {

        this.childSources.add(ChildSource(entityDef, rootForeignKey, watchedFields.toList()))

    }


    /**
     * An entity that the source entity reaches by following the foreign keys in [path] (dot-separated field names,
     * for example `"customer.region"`). A change to one of [watchedFields] (by default every field except the primary
     * key and audit fields), or the deletion of a row, refreshes the document of every source row that reaches the
     * changed row. The generator renders a `findRootIdsVia<Entity>` query for that; it needs no hand-written code.
     * Declaring the same entity more than once with different paths refreshes the documents reached by any of them.
     */
    fun watchesVia(entityDef: EntityDef, path: String, vararg watchedFields: String) {

        this.viaSources.add(ViaSource(entityDef, { path.split(".").forEach { forward(it) } }, path, watchedFields.toList()))

    }


    /**
     * Like [watchesVia] with a path of foreign-key steps, for paths that [watchesVia] with a string cannot express:
     * ```
     * watchesVia(caaRegistration, "tailNumber") {
     *     reverse(aircraftToCaaRegistration, "aircraft")   // rows of AircraftToCaaRegistration that reference this row
     *     forward("caaRegistration")
     * }
     * ```
     */
    fun watchesVia(entityDef: EntityDef, vararg watchedFields: String, path: ViaPathBuilder.() -> Unit) {

        this.viaSources.add(ViaSource(entityDef, path, "<custom path>", watchedFields.toList()))

    }


    /**
     * Throws if the declared refresh sources are inconsistent with [autoRefresh]; called by `AbstractSpec.esDoc`.
     */
    fun refreshDependencies(esDocDescription: String): List<EsRefreshDependencyDef> {

        val hasSources = this.sourceEntityDef != null || this.subclassSources.isNotEmpty() ||
            this.childSources.isNotEmpty() || this.viaSources.isNotEmpty()

        if (this.autoRefresh != true) {
            require(hasSources == false) {
                "EsDoc $esDocDescription declares refresh sources (sourceEntity/watchesSubclass/watchesChild/watchesVia) but not autoRefresh(true)."
            }
            return emptyList()
        }

        val rootEntityDef = requireNotNull(this.sourceEntityDef) {
            "autoRefresh(true) on hand-built EsDoc $esDocDescription requires sourceEntity(...) to say which entity's id is the EsDoc's id."
        }

        val rootBuilder = listOf(
            dependency(
                esDocDescription, rootEntityDef, isRoot = true, watchedFields = this.sourceEntityWatchedFields,
                refreshOnAnyUpdate = this.sourceEntityWatchedFields.isEmpty(), isChild = false, paths = listOf(emptyList()),
                role = EsRefreshRole.ROOT
            )
        )

        val subclasses = this.subclassSources.map { source ->
            require(isSubclassOf(source.entityDef, rootEntityDef)) {
                "EsDoc $esDocDescription: ${source.entityDef.entityBaseName} is not a subclass of ${rootEntityDef.entityBaseName}; use watchesChild()."
            }
            dependency(
                esDocDescription, source.entityDef, isRoot = false, watchedFields = source.watchedFields,
                refreshOnAnyUpdate = source.watchedFields.isEmpty(), isChild = false, paths = listOf(emptyList()),
                role = EsRefreshRole.ROOT
            )
        }

        val children = this.childSources.map { source ->

            val foreignKey = source.entityDef.findFieldByNameOrNull(source.rootForeignKey)
                ?: throw IllegalArgumentException("EsDoc $esDocDescription: ${source.entityDef.entityBaseName} has no field '${source.rootForeignKey}'.")

            val foreignEntityDef = (foreignKey.fieldType as? ForeignKeyFieldType)?.foreignKeyFieldDef?.foreignEntityDef
            require(foreignEntityDef != null && (foreignEntityDef == rootEntityDef || isSubclassOf(rootEntityDef, foreignEntityDef))) {
                "EsDoc $esDocDescription: ${source.entityDef.entityBaseName}.${source.rootForeignKey} must be a foreign key to ${rootEntityDef.entityBaseName}."
            }
            val watchedFields = source.watchedFields.ifEmpty {
                source.entityDef.allEntityFieldsSorted
                    .filterNot { it.isPrimaryKey.value }
                    .filterNot { it.classFieldName in auditFieldNames }
                    .map { it.classFieldName.value }
            }

            val hop = JoinHop(rootEntityDef, rootEntityDef.primaryKeyFields.single(), source.entityDef, foreignKey)

            dependency(
                esDocDescription, source.entityDef, isRoot = false, watchedFields = watchedFields,
                refreshOnAnyUpdate = false, isChild = true, paths = listOf(listOf(hop)), role = EsRefreshRole.CHILD
            )

        }

        val vias = this.viaSources.groupBy { it.entityDef }.map { (entityDef, sources) ->

            val paths = sources.map { source -> hopsAlong(esDocDescription, rootEntityDef, entityDef, source) }.distinct()

            val watchedFields = sources.flatMap { it.watchedFields }.distinct().ifEmpty {
                entityDef.allEntityFieldsSorted
                    .filterNot { it.isPrimaryKey.value }
                    .filterNot { it.classFieldName in auditFieldNames }
                    .map { it.classFieldName.value }
            }

            dependency(
                esDocDescription, entityDef, isRoot = false, watchedFields = watchedFields,
                refreshOnAnyUpdate = false, isChild = false, paths = paths, role = EsRefreshRole.PATH
            )

        }

        val all = rootBuilder + subclasses + children + vias

        all.groupBy { it.entityDef }.forEach { (entityDef, dependencies) ->
            require(dependencies.size == 1) {
                "EsDoc $esDocDescription: entity ${entityDef.entityBaseName} is declared as a refresh source more than once " +
                    "(sourceEntity/watchesSubclass/watchesChild/watchesVia); each entity can be declared once."
            }
        }

        return all

    }


    /** The hops from [rootEntityDef] along the source's steps, which must end at [entityDef]. */
    private fun hopsAlong(esDocDescription: String, rootEntityDef: EntityDef, entityDef: EntityDef, source: ViaSource): List<JoinHop> {

        val pathBuilder = ViaPathBuilder(esDocDescription, rootEntityDef)
        source.steps(pathBuilder)

        require(pathBuilder.current == entityDef) {
            "EsDoc $esDocDescription: path '${source.description}' from ${rootEntityDef.entityBaseName} ends at ${pathBuilder.current.entityBaseName}, not ${entityDef.entityBaseName}."
        }

        return pathBuilder.hops

    }


    private fun isSubclassOf(entityDef: EntityDef, ancestor: EntityDef): Boolean {

        var current = entityDef.superclassEntityDef

        while (current != null) {
            if (current == ancestor) return true
            current = current.superclassEntityDef
        }

        return false

    }


    private fun dependency(
        esDocDescription: String,
        entityDef: EntityDef,
        isRoot: Boolean,
        watchedFields: List<String>,
        refreshOnAnyUpdate: Boolean,
        isChild: Boolean,
        paths: List<List<JoinHop>>,
        role: EsRefreshRole
    ): EsRefreshDependencyDef {

        watchedFields.forEach {
            require(entityDef.findFieldByNameOrNull(it) != null) {
                "EsDoc $esDocDescription: ${entityDef.entityBaseName} has no field '$it' to watch."
            }
        }

        require(entityDef.crudDef.withCrudListener.value) {
            "EsDoc $esDocDescription: auto-refresh requires entity ${entityDef.entityBaseName} to have a CrudListener (declare a crud {} block on it)."
        }

        require(entityDef.primaryKeyFields.singleOrNull()?.classFieldDef?.fieldType is DomainIdFieldType) {
            "EsDoc $esDocDescription: entity ${entityDef.entityBaseName} requires a single DomainId primary key."
        }

        return EsRefreshDependencyDef(
            entityDef,
            isRoot,
            watchedFields.map { ClassFieldName(it) }.toSet(),
            paths,
            refreshOnAnyUpdate,
            isManyToManyMembership = isChild,
            role = role
        )

    }


    fun build(): EsDocDef {

        return EsDocDef(
            this.packageName,
            this.esDocBaseName,
            this.elasticIndexBaseName,
            this.esDocVersion,
            this.indexDescription,
            this.fieldDefs,
            this.renderFieldEnum,
            this.generateRefreshIndexJob,
            this.disableRendering,
            entityDef = null,
            refreshDependencies = refreshDependencies("${this.packageName}.${this.esDocBaseName} V${this.esDocVersion}")
        )

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = fieldType,
        )

        init?.invoke(fieldBuilder)

        this.fieldDefs.add(fieldBuilder.build())

    }


    fun field(
        fieldName: String,
        valueClassDef: StringValueClassDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.stringValueClass(valueClassDef),
        )

        init?.invoke(fieldBuilder)

        this.fieldDefs.add(fieldBuilder.build())

    }


    fun field(
        fieldName: String,
        esDocDef: EsDocDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.esDoc(esDocDef),
        )

        init?.invoke(fieldBuilder)
        this.fieldDefs.add(fieldBuilder.build())

    }


    fun field(
        fieldName: String,
        enumDef: EnumDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.enum(enumDef),
        )

        init?.invoke(fieldBuilder)

        this.fieldDefs.add(fieldBuilder.build())

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.stringType(stringTypeDef),
        )

        init?.invoke(fieldBuilder)

        this.fieldDefs.add(fieldBuilder.build())

    }


}


/** The steps of a [EsDocDefBuilder.watchesVia] path, from the source entity to the watched entity. */
@MaiaDslMarker
class ViaPathBuilder internal constructor(
    private val esDocDescription: String,
    rootEntityDef: EntityDef
) {


    internal var current: EntityDef = rootEntityDef
        private set


    internal val hops = mutableListOf<JoinHop>()


    /**
     * Follows the foreign key [field] of the current entity to the entity it references, or to [asSubclass] when the
     * key references an abstract superclass and only that subclass is watched.
     */
    fun forward(field: String, asSubclass: EntityDef? = null) {

        val fieldDef = this.current.findFieldByNameOrNull(field)
            ?: throw IllegalArgumentException("EsDoc $esDocDescription: ${current.entityBaseName} has no field '$field'.")

        val referenced = (fieldDef.fieldType as? ForeignKeyFieldType)?.foreignKeyFieldDef?.foreignEntityDef
            ?: throw IllegalArgumentException("EsDoc $esDocDescription: ${current.entityBaseName}.$field is not a foreign key.")

        val target = asSubclass ?: referenced

        require(target == referenced || isSubclassOf(target, referenced)) {
            "EsDoc $esDocDescription: ${target.entityBaseName} is not ${referenced.entityBaseName} or a subclass of it."
        }

        add(fieldDef, target)

    }


    /** Steps to the rows of [childEntityDef] whose foreign key [childForeignKey] references the current entity. */
    fun reverse(childEntityDef: EntityDef, childForeignKey: String) {

        val fieldDef = childEntityDef.findFieldByNameOrNull(childForeignKey)
            ?: throw IllegalArgumentException("EsDoc $esDocDescription: ${childEntityDef.entityBaseName} has no field '$childForeignKey'.")

        val referenced = (fieldDef.fieldType as? ForeignKeyFieldType)?.foreignKeyFieldDef?.foreignEntityDef
        require(referenced != null && (referenced == current || isSubclassOf(current, referenced))) {
            "EsDoc $esDocDescription: ${childEntityDef.entityBaseName}.$childForeignKey must be a foreign key to ${current.entityBaseName}."
        }

        val currentPk = primaryKeyOf(current)
        hops.add(JoinHop(current, currentPk, childEntityDef, fieldDef))
        current = childEntityDef

    }


    private fun add(fromField: EntityFieldDef, target: EntityDef) {

        hops.add(JoinHop(current, fromField, target, primaryKeyOf(target)))
        current = target

    }


    private fun primaryKeyOf(entityDef: EntityDef): EntityFieldDef {

        return entityDef.primaryKeyFields.singleOrNull()
            ?: throw IllegalArgumentException("EsDoc $esDocDescription: ${entityDef.entityBaseName} needs a single primary key field to be part of a watchesVia path.")

    }


    private fun isSubclassOf(entityDef: EntityDef, ancestor: EntityDef): Boolean {

        var c = entityDef.superclassEntityDef

        while (c != null) {
            if (c == ancestor) return true
            c = c.superclassEntityDef
        }

        return false

    }


}
