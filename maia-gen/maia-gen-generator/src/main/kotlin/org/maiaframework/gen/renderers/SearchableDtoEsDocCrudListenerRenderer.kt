package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.AnnotationDefs
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EsRefreshDependencyDef
import org.maiaframework.gen.spec.definition.EsRefreshRole
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


/**
 * Renders a Spring bean that keeps one auto-refreshed EsDoc version of a searchable DTO in step with one of its
 * [EsRefreshDependencyDef]s: it implements the dependency entity's CrudListener and enlists the affected root ids
 * with the EsRefreshCollector. It overrides exactly the members of that CrudListener interface (see
 * [CrudListenerCallbacks]). Use [EsRefreshListeners.renderersFor] to get the full set for a searchable DTO.
 */
class SearchableDtoEsDocCrudListenerRenderer(
    private val searchableDtoDef: SearchableDtoDef,
    private val esDocDef: EsDocDef,
    val dependency: EsRefreshDependencyDef
) : AbstractKotlinRenderer(
    esRefreshListenerClassDef(searchableDtoDef, esDocDef, dependency)
) {


    private val entityDef = this.dependency.entityDef


    private val entityUqcn = this.entityDef.entityUqcn


    private val role = this.dependency.role


    private val reverseJoin: EsReverseJoins.ReverseJoin? = if (this.role == EsRefreshRole.ROOT || this.role == EsRefreshRole.JOIN) {
        null
    } else {
        EsReverseJoins.reverseJoinsFor(this.searchableDtoDef).singleOrNull { it.dependency === this.dependency }
            ?: throw IllegalArgumentException(
                "No findRootIdsVia${this.entityDef.entityBaseName} reverse join is rendered for searchable DTO ${this.searchableDtoDef.dtoBaseName}; " +
                    "cannot render an EsDoc auto-refresh listener for it"
            )
    }


    private val idProperty = if (this.role == EsRefreshRole.JOIN) {
        null
    } else {
        (this.reverseJoin?.primaryKeyField ?: this.entityDef.primaryKeyFields.single()).classFieldName
    }


    /** On a many-to-many join entity, its foreign key to the root: the root id itself. */
    private val rootForeignKeyField: EntityFieldDef? = if (this.role == EsRefreshRole.JOIN) {
        EsRefreshListeners.rootForeignKeyFieldOf(this.dependency)
            ?: throw IllegalArgumentException(
                "Join entity ${this.entityDef.entityBaseName} on searchable DTO ${this.searchableDtoDef.dtoBaseName} has no single DomainId foreign key to the root; " +
                    "cannot render an EsDoc auto-refresh listener for it"
            )
    } else {
        null
    }


    private val rootForeignKeyProperty = this.rootForeignKeyField?.classFieldName


    /**
     * A nullable root foreign key is handled with `listOfNotNull`. The DSL cannot currently build one (a
     * many-to-many entity always declares its left/right foreign keys as non-null), hence no test.
     */
    private val listOfRootIds = if (this.rootForeignKeyField?.classFieldDef?.nullable == true) "listOfNotNull" else "listOf"


    private val watchedFieldNames = this.dependency.watchedFields.map { it.value }.sorted()


    init {

        addConstructorArg(aClassField("collector", Fqcns.ES_REFRESH_COLLECTOR).privat().build())

        if (this.reverseJoin != null) {
            addConstructorArg(aClassField("repo", this.searchableDtoDef.dtoRepoClassDef.fqcn).privat().build())
        }

        addConstructorArg(aClassField("indexService", this.esDocDef.indexServiceClassDef.fqcn).privat().build())

    }


    override fun renderSource(): String {

        val marker = "\n@Service\n"
        val rendered = super.renderSource()

        check(rendered.contains(marker)) { "Could not inject class KDoc into ${this.classDef.fqcn}: expected annotation marker not found" }

        return rendered.replaceFirst(marker, "\n${classKdoc()}@Service\n")

    }


    private fun classKdoc(): String {

        val indexService = this.esDocDef.indexServiceClassDef.uqcn

        val trigger = when (this.role) {
            EsRefreshRole.ROOT -> if (this.dependency.refreshOnAnyUpdate) {
                "creating, updating or deleting a row refreshes its own document. Every update refreshes it, because the\n" +
                    " * row's many-to-many membership may change in the same operation."
            } else {
                "creating or deleting a row refreshes its own document, and an update refreshes it when a field the EsDoc\n" +
                    " * reads changes."
            }
            EsRefreshRole.PATH -> "when a field the EsDoc reads changes, or before a row is deleted, the documents of every root\n" +
                " * that reaches the row are refreshed."
            EsRefreshRole.JOIN -> "creating a join row, changing a field the EsDoc reads, or deleting it refreshes the\n" +
                " * document of the root it joins (before and after, if the row moves to another root). The root id is read\n" +
                " * straight from the join row's foreign key to the root, so no query is needed."
            EsRefreshRole.OTHER_SIDE -> "every update, and every delete, refreshes the documents of the roots the row is\n" +
                " * joined to."
        }

        val twoPhase = if (this.role == EsRefreshRole.OTHER_SIDE) {
            """
            | *
            | * Two-phase rule: the many-to-many CrudServices reconcile join rows after the update event has fired, so an
            | * update may coincide with a membership change this listener cannot see. The roots joined before the update are
            | * resolved now and enlisted, and the roots joined after it are resolved after commit, from the id captured now.
            |""".trimMargin()
        } else {
            ""
        }

        return """
            |/**
            | * Generated. Keeps [$indexService] in step with changes to [${this.entityUqcn}] that raise CrudListener events:
            | * $trigger
            | * Ids are enlisted with [EsRefreshCollector], which refreshes them once the transaction has committed.
            |$twoPhase *
            | * Changes that raise no CrudListener event (importers, direct DAO writes, join rows edited outside a CrudService
            | * event) are not seen here; the scheduled job calling [$indexService.refreshIndex] reconciles them.
            | */
            |""".trimMargin()

    }


    override fun renderFunctions() {

        addImportFor(this.entityDef.entityFqcn)

        `render function onCreated`()
        `render function onUpdated`()
        `render functions onBeforeDelete and onDeleted`()

    }


    private fun `render function onCreated`() {

        if (CrudListenerCallbacks.hasCreateCallback(this.entityDef) == false) {
            return
        }

        val body = when (this.role) {
            EsRefreshRole.ROOT -> listOf(enlistOwnId("entity"))
            EsRefreshRole.JOIN -> listOf(enlistRootForeignKey("entity"))
            // a new row cannot yet be referenced by any root
            EsRefreshRole.PATH, EsRefreshRole.OTHER_SIDE -> emptyList()
        }

        renderOverride("on${this.entityUqcn}Created(entity: ${this.entityUqcn})", body)

    }


    private fun `render function onUpdated`() {

        if (CrudListenerCallbacks.hasUpdateCallback(this.entityDef) == false) {
            return
        }

        val body = when (this.role) {
            EsRefreshRole.ROOT -> if (this.dependency.refreshOnAnyUpdate) {
                listOf(enlistOwnId("after"))
            } else {
                ifWatchedFieldsChanged(listOf(enlistOwnId("after")))
            }
            // an update does not change the foreign key path from a root to the row, so the roots can be
            // resolved from committed state, outside the caller's transaction
            EsRefreshRole.PATH -> ifWatchedFieldsChanged(enlistRootsAfterCommit())
            EsRefreshRole.JOIN -> ifWatchedFieldsChanged(listOf(
                "        this.collector.enlistRefresh(this.indexService, ${this.listOfRootIds}(before.${this.rootForeignKeyProperty}, after.${this.rootForeignKeyProperty}).distinct())"
            ))
            EsRefreshRole.OTHER_SIDE -> listOf(enlistRootsNow("before")) + enlistRootsAfterCommit()
        }

        renderOverride("on${this.entityUqcn}Updated(before: ${this.entityUqcn}, after: ${this.entityUqcn})", body)

    }


    private fun `render functions onBeforeDelete and onDeleted`() {

        if (CrudListenerCallbacks.hasDeleteCallback(this.entityDef) == false) {
            return
        }

        // path and other-side rows are resolved before the delete, while the rows referencing them still exist
        val beforeDeleteBody = when (this.role) {
            EsRefreshRole.PATH, EsRefreshRole.OTHER_SIDE -> listOf(enlistRootsNow("entity"))
            EsRefreshRole.ROOT, EsRefreshRole.JOIN -> emptyList()
        }

        // the index service deletes the documents of ids whose row no longer exists
        val deletedBody = when (this.role) {
            EsRefreshRole.ROOT -> listOf(enlistOwnId("entity"))
            EsRefreshRole.JOIN -> listOf(enlistRootForeignKey("entity"))
            EsRefreshRole.PATH, EsRefreshRole.OTHER_SIDE -> emptyList()
        }

        renderOverride("on${this.entityUqcn}BeforeDelete(entity: ${this.entityUqcn})", beforeDeleteBody)
        renderOverride("on${this.entityUqcn}Deleted(entity: ${this.entityUqcn})", deletedBody)

    }


    private fun enlistOwnId(variableName: String): String {

        return "        this.collector.enlistRefresh(this.indexService, listOf($variableName.${this.idProperty}))"

    }


    private fun enlistRootForeignKey(variableName: String): String {

        return "        this.collector.enlistRefresh(this.indexService, ${this.listOfRootIds}($variableName.${this.rootForeignKeyProperty}))"

    }


    /**
     * For rows that must be resolved before the change commits (the roots joined to a row about to be deleted, or
     * the old members of a many-to-many). A failing lookup is logged by the collector and does not fail the caller.
     */
    private fun enlistRootsNow(variableName: String): String {

        return "        this.collector.enlistRefreshNow(this.indexService) { this.repo.${this.reverseJoin!!.functionName}($variableName.${this.idProperty}) }"

    }


    private fun enlistRootsAfterCommit(): List<String> {

        return listOf(
            "        val afterId = after.${this.idProperty}",
            "        this.collector.enlistRefreshResolved(this.indexService) { this.repo.${this.reverseJoin!!.functionName}(afterId) }"
        )

    }


    private fun ifWatchedFieldsChanged(statements: List<String>): List<String> {

        if (this.watchedFieldNames.isEmpty()) {
            return emptyList()
        }

        val condition = this.watchedFieldNames.joinToString(" || ") { "before.$it != after.$it" }

        return listOf("        if ($condition) {") + statements.map { "    $it" } + "        }"

    }


    private fun renderOverride(signature: String, bodyLines: List<String>) {

        blankLine()
        blankLine()

        if (bodyLines.isEmpty()) {
            appendLine("    override fun $signature {}")
            return
        }

        appendLine("    override fun $signature {")
        blankLine()
        bodyLines.forEach { appendLine(it) }
        blankLine()
        appendLine("    }")

    }


}


private fun esRefreshListenerClassDef(
    searchableDtoDef: SearchableDtoDef,
    esDocDef: EsDocDef,
    dependency: EsRefreshDependencyDef
): ClassDef {

    val className = "${esDocDef.esDocBaseName}V${esDocDef.esDocVersion}EsRefresh${dependency.entityDef.entityBaseName}CrudListener"

    return aClassDef(searchableDtoDef.packageName.uqcn(className))
        .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
        .withInterface(ParameterizedType(dependency.entityDef.crudListenerClassDef.fqcn))
        .build()

}
