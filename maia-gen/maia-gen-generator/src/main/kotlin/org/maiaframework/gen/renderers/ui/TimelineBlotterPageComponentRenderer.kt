package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class TimelineBlotterPageComponentRenderer(
    private val def: TimelineBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core/rxjs-interop", "toSignal")
        addImport("@angular/router", "ActivatedRoute")
        addImport("rxjs", "map")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(TypescriptImport(
            def.blotterComponentNames.componentName,
            "@$genDir/${def.blotterComponentNames.componentNameKebab}"
        ))
    }


    override fun renderedFilePath(): String {
        return def.blotterPageComponentNames.componentRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("    changeDetection: ChangeDetectionStrategy.OnPush,")
        appendLine("    imports: [PageLayout, ${def.blotterComponentNames.componentName}],")
        appendLine("    selector: '${def.blotterPageComponentNames.componentSelector}',")
        appendLine("    templateUrl: './${def.blotterPageComponentNames.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${def.blotterPageComponentNames.componentName} {")
        blankLine()
        blankLine()
        appendLine("    private readonly route = inject(ActivatedRoute);")
        blankLine()
        blankLine()
        appendLine("    protected readonly entityId = toSignal(")
        appendLine("        this.route.paramMap.pipe(map(p => p.get('id')))")
        appendLine("    );")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
