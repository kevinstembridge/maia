package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir


class EntityCrudRoutesRenderer(
    private val entityDef: EntityDef,
    private val blotterPageDef: BlotterPageDef? = null,
    private val entityDetailViewDef: EntityDetailViewDef? = null,
    private val entityCreatePageDef: EntityCreatePageDef? = null,
    private val entityEditPageDef: EntityEditPageDef? = null,
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(entityDef.packageName)


    private val entityBaseName = entityDef.entityBaseName


    private val constName = "${entityBaseName.firstToLower()}Routes"


    init {
        addImport("@angular/router", "Routes")
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
        entityCreatePageDef?.let { renderCreateRoute(it) }
        entityEditPageDef?.let { renderEditRoute(it) }
        appendLine("];")

    }


    private fun renderBlotterRoute(def: BlotterPageDef) {

        append("""
            |    {
            |        path: '${def.routePath}',
            |        loadComponent: () =>
            |            import('./${def.pageAngularComponentNames.componentNameKebab}').then(m => m.${def.pageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderViewRoute(def: EntityDetailViewDef) {

        val path = def.viewPageUrl.removePrefix("/") + "/:id"

        append("""
            |    {
            |        path: '$path',
            |        loadComponent: () =>
            |            import('./${def.viewPageAngularComponentNames.componentNameKebab}').then(m => m.${def.viewPageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderCreateRoute(def: EntityCreatePageDef) {

        val path = def.createPageUrl.removePrefix("/")

        append("""
            |    {
            |        path: '$path',
            |        loadComponent: () =>
            |            import('./${def.createPageAngularComponentNames.componentNameKebab}').then(m => m.${def.createPageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderEditRoute(def: EntityEditPageDef) {

        val path = def.entityDef.editEntityPageUrl.removePrefix("/") + "/:id"

        append("""
            |    {
            |        path: '$path',
            |        loadComponent: () =>
            |            import('./${def.editPageAngularComponentNames.componentNameKebab}').then(m => m.${def.editPageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


    private fun renderHistoryRoute(def: EntityHistoryBlotterDef) {

        append("""
            |    {
            |        path: '${def.routePath}/:id',
            |        loadComponent: () =>
            |            import('./${def.blotterPageComponentNames.componentNameKebab}').then(m => m.${def.blotterPageComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }


}
