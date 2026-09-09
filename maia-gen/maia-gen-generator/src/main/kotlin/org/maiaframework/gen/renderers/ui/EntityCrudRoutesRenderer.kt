package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.TimelineBlotterDef


class EntityCrudRoutesRenderer(
    private val entityDef: EntityDef,
    private val authoritiesDef: AuthoritiesDef? = null,
    private val blotterPageDef: BlotterPageDef? = null,
    private val entityDetailViewDef: EntityDetailViewDef? = null,
    private val entityCreatePageDef: EntityCreatePageDef? = null,
    private val entityEditPageDef: EntityEditPageDef? = null,
    private val timelineBlotterDef: TimelineBlotterDef? = null,
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(entityDef.packageName)


    private val entityBaseName = entityDef.entityBaseName


    private val constName = "${entityBaseName.firstToLower()}Routes"


    private val routeAuthority = entityDef.crudDef.authority


    // Empty when there's no authority to require: an empty `data.authorities` array denies
    // everyone (including logged-in users), so it must never be rendered as `authorities: []`.
    private val dataLine = if (routeAuthority != null && authoritiesDef != null) {
        "\n        data: {authorities: [${authoritiesDef.enumDef.uqcn}.${routeAuthority.name}]},"
    } else {
        ""
    }


    init {
        addImport("@angular/router", "Routes")
        if (routeAuthority != null) {
            authoritiesDef?.let { addImport(it.enumDef.typescriptImport) }
        }
    }


    override fun renderedFilePath(): String {

        return "$genDir/${entityBaseName.toKebabCase()}-routes.ts"

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export const $constName: Routes = [")
        blotterPageDef?.let { renderBlotterRoute(it) }
        entityDetailViewDef?.let { renderViewRoute(it) }
        entityDef.historyBlotterDef?.let { renderHistoryRoute(it) }
        timelineBlotterDef?.let { renderTimelineRoute(it) }
        entityCreatePageDef?.let { renderCreateRoute(it) }
        entityEditPageDef?.let { renderEditRoute(it) }
        appendLine("];")

    }


    private fun renderBlotterRoute(def: BlotterPageDef) {

        append("""
            |    {
            |        path: '${def.routePath}',$dataLine
            |        loadComponent: () =>
            |            import('./${def.pageAngularComponentNames.componentNameKebab}').then(m => m.${def.pageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderViewRoute(def: EntityDetailViewDef) {

        val path = def.viewPageUrl.removePrefix("/") + "/:id"

        append("""
            |    {
            |        path: '$path',$dataLine
            |        loadComponent: () =>
            |            import('./${def.viewPageAngularComponentNames.componentNameKebab}').then(m => m.${def.viewPageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderCreateRoute(def: EntityCreatePageDef) {

        val path = def.createPageUrl.removePrefix("/")

        append("""
            |    {
            |        path: '$path',$dataLine
            |        loadComponent: () =>
            |            import('./${def.createPageAngularComponentNames.componentNameKebab}').then(m => m.${def.createPageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderEditRoute(def: EntityEditPageDef) {

        val path = def.entityDef.editEntityPageUrl.removePrefix("/") + "/:id"

        append("""
            |    {
            |        path: '$path',$dataLine
            |        loadComponent: () =>
            |            import('./${def.editPageAngularComponentNames.componentNameKebab}').then(m => m.${def.editPageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderHistoryRoute(def: EntityHistoryBlotterDef) {

        val path = if (def.isJoinEntityHistory) def.routePath else "${def.routePath}/:id"

        append("""
            |    {
            |        path: '$path',$dataLine
            |        loadComponent: () =>
            |            import('./${def.blotterPageComponentNames.componentNameKebab}').then(m => m.${def.blotterPageComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderTimelineRoute(def: TimelineBlotterDef) {

        val path = "${def.routePath}/:id"

        append("""
            |    {
            |        path: '$path',$dataLine
            |        loadComponent: () =>
            |            import('./${def.blotterPageComponentNames.componentNameKebab}').then(m => m.${def.blotterPageComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


}
