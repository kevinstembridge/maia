package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailViewPageComponentRenderer(
    private val entityDetailViewDef: EntityDetailViewDef,
    private val authoritiesDef: AuthoritiesDef?,
    private val blotterPageDef: BlotterPageDef?,
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "inject")
        addImport("@angular/core/rxjs-interop", "toSignal")
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/material/icon", "MatIconModule")
        addImport("@angular/router", "ActivatedRoute")
        addImport("@angular/router", "Router")
        addImport("rxjs", "map")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(this.entityDetailViewDef.viewContentAngularComponentNames.componentTypescriptImport)

        authoritiesDef?.let {
            addImport(it.authServiceTypescriptImport)
            addImport(it.enumDef.typescriptImport)
        }

    }


    override fun renderedFilePath(): String {

        return this.entityDetailViewDef.viewPageAngularComponentNames.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [
            |        MatButtonModule,
            |        MatIconModule,
            |        PageLayout,
            |        ${this.entityDetailViewDef.viewContentAngularComponentNames.componentName}
            |    ],
            |    selector: '${this.entityDetailViewDef.viewPageAngularComponentNames.componentSelector}',
            |    templateUrl: './${this.entityDetailViewDef.viewPageAngularComponentNames.htmlFileName}'
            |})
            |export class ${this.entityDetailViewDef.viewPageAngularComponentNames.componentName} {
            |
            |
            |    private readonly route = inject(ActivatedRoute);
            |
            |
            |    private readonly router = inject(Router);
            |
            |
            |    private readonly authService = inject(AuthService);
            |
            |
            |    protected readonly entityId = toSignal(
            |        this.route.paramMap.pipe(
            |            map(p => p.get('id'))
            |        )
            |    );
            |""".trimMargin())

        this.authoritiesDef?.let {

            append("""
                |
                |
                |    protected get canEdit(): boolean {
                |        return this.authService.currentUserHasThisAuthority(${it.enumDef.uqcn}.${entityDetailViewDef.editPermission!!.name});
                |    }
                |
                |
                |    onEditClicked(): void {
                |        const id = this.entityId();
                |        if (id) {
                |            this.router.navigate(['${this.entityDetailViewDef.editPageUrl}', id]);
                |        }
                |    }
                |""".trimMargin())

        }

        this.blotterPageDef?.let {

            append("""
                |
                |
                |    onBlotterClicked(): void {
                |        this.router.navigate(['/${it.routePath}']);
                |    }
                |""".trimMargin())

        }

        append("""
            |
            |
            |}
            |""".trimMargin())

    }


}
