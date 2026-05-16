package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailViewPageComponentRenderer(
    private val entityDetailViewDef: EntityDetailViewDef
) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")

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
            |
            |
            |    protected get canEdit(): boolean {
            |        return this.authService.currentUserHasThisAuthority(Authority.WRITE);
            |    }
            |
            |
            |    onEditClicked(): void {
            |        const id = this.entityId();
            |        if (id) {
            |            this.router.navigate(['${this.entityDetailViewDef.editPageUrl}', id]);
            |        }
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}
