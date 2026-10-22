package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir


/**
 * Renders the route for a [BlotterPageDef] whose blotter is not backed by an EntityDef, and so
 * has no EntityCrudRoutesRenderer file to hold its route. The route carries no authority; it is
 * expected to be mounted under a parent route that supplies one.
 */
class BlotterRoutesRenderer(
    private val blotterPageDef: BlotterPageDef,
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(blotterPageDef.blotterDef.packageName)


    private val dtoBaseName = blotterPageDef.blotterDef.dtoBaseName


    private val constName = "${dtoBaseName.firstToLower()}BlotterRoutes"


    init {
        addImport("@angular/router", "Routes")
    }


    override fun renderedFilePath(): String {

        return "$genDir/${dtoBaseName.toKebabCase()}-blotter-routes.ts"

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export const $constName: Routes = [")
        append("""
            |    {
            |        path: '${blotterPageDef.routePath}',
            |        loadComponent: () =>
            |            import('./${blotterPageDef.pageAngularComponentNames.componentNameKebab}').then(m => m.${blotterPageDef.pageAngularComponentNames.componentName}),
            |    },
            |""".trimMargin())
        appendLine("];")

    }


}
