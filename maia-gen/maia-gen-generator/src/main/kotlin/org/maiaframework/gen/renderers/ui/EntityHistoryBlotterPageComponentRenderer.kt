package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterPageComponentRenderer(
    private val def: EntityHistoryBlotterDef
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

        append("""
            |
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [PageLayout, ${def.blotterComponentNames.componentName}],
            |    selector: '${def.blotterPageComponentNames.componentSelector}',
            |    templateUrl: './${def.blotterPageComponentNames.htmlFileName}'
            |})
            |export class ${def.blotterPageComponentNames.componentName} {
            |
            |
            |    private readonly route = inject(ActivatedRoute);
            |
            |
            |    protected readonly entityId = toSignal(
            |        this.route.paramMap.pipe(map(p => p.get('id')))
            |    );
            |
            |
            |}
            |""".trimMargin())

    }


}
