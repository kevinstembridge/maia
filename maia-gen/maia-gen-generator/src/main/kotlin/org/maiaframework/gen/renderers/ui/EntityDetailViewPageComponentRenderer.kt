package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.TimelineBlotterDef

class EntityDetailViewPageComponentRenderer(
    private val entityDetailViewDef: EntityDetailViewDef,
    private val authoritiesDef: AuthoritiesDef?,
    private val blotterPageDef: BlotterPageDef?,
    private val timelineBlotterDef: TimelineBlotterDef? = null,
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
        addImport("@maia/maia-ui", "PageLayout")
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

        val editPermission = this.entityDetailViewDef.editPermission

        if (this.authoritiesDef != null && editPermission != null) {

            append("""
                |
                |
                |    protected get canEdit(): boolean {
                |        return this.authService.currentUserHasThisAuthority(${this.authoritiesDef.enumDef.uqcn}.${editPermission.name});
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

        } else {

            append("""
                |
                |
                |    protected readonly canEdit = false;
                |
                |
                |    onEditClicked(): void {
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

        entityDetailViewDef.entityDef.historyBlotterDef?.let { historyDef ->
            if (historyDef.isJoinEntityHistory) {
                // The history route of a join entity shows the whole history table and takes no id.
                append("""
                    |
                    |
                    |    onHistoryClicked(): void {
                    |        this.router.navigate(['/${historyDef.routePath}']);
                    |    }
                    |""".trimMargin())
            } else {
                append("""
                    |
                    |
                    |    onHistoryClicked(): void {
                    |        const id = this.entityId();
                    |        if (id) {
                    |            this.router.navigate(['/${historyDef.routePath}', id]);
                    |        }
                    |    }
                    |""".trimMargin())
            }
        }

        timelineBlotterDef?.let { timelineDef ->
            append("""
                |
                |
                |    onTimelineClicked(): void {
                |        const id = this.entityId();
                |        if (id) {
                |            this.router.navigate(['/${timelineDef.routePath}', id]);
                |        }
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
