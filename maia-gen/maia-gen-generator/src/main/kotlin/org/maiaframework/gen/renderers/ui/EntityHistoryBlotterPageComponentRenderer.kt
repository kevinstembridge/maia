package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterPageComponentRenderer(
    private val def: EntityHistoryBlotterDef,
    private val viewPageDef: EntityDetailViewDef?
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    private val showsViewButton = !def.isJoinEntityHistory && viewPageDef != null


    init {
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(TypescriptImport(
            def.blotterComponentNames.componentName,
            "@$genDir/${def.blotterComponentNames.componentNameKebab}"
        ))

        if (!def.isJoinEntityHistory) {
            addImport("@angular/core", "inject")
            addImport("@angular/core/rxjs-interop", "toSignal")
            addImport("@angular/router", "ActivatedRoute")
            addImport("rxjs", "map")
        }

        if (showsViewButton) {
            addImport("@angular/router", "Router")
            addImport("@angular/material/button", "MatButtonModule")
            addImport("@angular/material/icon", "MatIconModule")
        }

    }


    override fun renderedFilePath(): String {
        return def.blotterPageComponentNames.componentRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("    changeDetection: ChangeDetectionStrategy.OnPush,")
        if (showsViewButton) {
            appendLine("    imports: [MatButtonModule, MatIconModule, PageLayout, ${def.blotterComponentNames.componentName}],")
        } else {
            appendLine("    imports: [PageLayout, ${def.blotterComponentNames.componentName}],")
        }
        appendLine("    selector: '${def.blotterPageComponentNames.componentSelector}',")
        appendLine("    templateUrl: './${def.blotterPageComponentNames.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${def.blotterPageComponentNames.componentName} {")

        if (def.isJoinEntityHistory) {

            appendLine("}")

        } else {

            blankLine()
            blankLine()
            appendLine("    private readonly route = inject(ActivatedRoute);")

            if (showsViewButton) {
                blankLine()
                blankLine()
                appendLine("    private readonly router = inject(Router);")
            }

            blankLine()
            blankLine()
            appendLine("    protected readonly entityId = toSignal(")
            appendLine("        this.route.paramMap.pipe(map(p => p.get('id')))")
            appendLine("    );")

            if (showsViewButton) {
                blankLine()
                blankLine()
                appendLine("    onViewClicked(): void {")
                appendLine("        const id = this.entityId();")
                appendLine("        if (id) {")
                appendLine("            this.router.navigate(['${viewPageDef!!.entityDef.viewEntityPageUrl}', id]);")
                appendLine("        }")
                appendLine("    }")
            }

            blankLine()
            blankLine()
            appendLine("}")

        }

    }


}
