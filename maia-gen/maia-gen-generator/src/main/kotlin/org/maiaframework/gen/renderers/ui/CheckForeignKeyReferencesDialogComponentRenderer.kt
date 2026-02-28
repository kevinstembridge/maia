package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.MaiaGenConstants


class CheckForeignKeyReferencesDialogComponentRenderer(private val entityDef: EntityDef) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "Inject")
        addImport("@angular/core", "OnInit")
        addImport("@angular/material/dialog", "MatDialog")
        addImport("@angular/material/dialog", "MatDialogRef")
        addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
        addImport("@angular/material/dialog", "MatDialogTitle")
        addImport("@angular/material/dialog", "MatDialogContent")
        addImport("@angular/material/dialog", "MatDialogActions")
        addImport("rxjs", "of")
        addImport("rxjs/operators", "catchError")
        addImport("rxjs/operators", "tap")
        addImport(MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_TYPESCRIPT_IMPORT)
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/material/progress-spinner", "MatProgressSpinnerModule")
        addImport("@maia/maia-ui", "MessageDetails")
        addImport("@maia/maia-ui", "MessagePanelComponent")

    }


    override fun renderedFilePath(): String {

        return entityDef.checkForeignKeyReferencesDialog.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""


            @Component({
                imports: [MatDialogTitle, MatDialogContent, MatProgressSpinnerModule, MessagePanelComponent, MatDialogActions, MatButtonModule],
                selector: '${this.entityDef.checkForeignKeyReferencesDialog.componentSelector}',
                templateUrl: './${this.entityDef.checkForeignKeyReferencesDialog.htmlFileName}'
            })
            export class ${this.entityDef.checkForeignKeyReferencesDialog.componentName} implements OnInit {
            
                
                checking: boolean = false;
                
                
                messageDetails = new MessageDetails();
            
                
                constructor(
                    public dialogRef: MatDialogRef<${this.entityDef.checkForeignKeyReferencesDialog.componentName}>,
                    @Inject(MAT_DIALOG_DATA) private dto: any,
                    private foreignKeyReferenceService: ${MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_CLASS_NAME}
                ) {}
            
            
                ngOnInit() {
            
                    this.foreignKeyReferenceService.check${this.entityDef.entityBaseName}ForReferences(this.dto.id)
                      .pipe(
                        tap(() => this.checking = true),
                        catchError(err => {
                          this.messageDetails.setErrorDetailsFromHttpError(err);
                          return of(null);
                        }),
                        tap(() => this.checking = false)
                      ).subscribe(
                        res => {
                          if (res?.exists) {
                            this.messageDetails.setErrorMessage('Foreign key references to entity ' + res.entityKey + ' exist.');
                          } else {
                            this.dialogRef.close(true);
                          }
                        }
                      );
            
                }
            
            
                onCancel() {
                    this.dialogRef.close();
                }
            
            
            }""".trimIndent())

    }


}
