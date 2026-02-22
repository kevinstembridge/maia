package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.MaiaGenConstants


class CheckForeignKeyReferencesDialogComponentRenderer(private val entityDef: EntityDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return entityDef.checkForeignKeyReferencesDialog.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            import { Component, Inject, OnInit } from '@angular/core';
            import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
            import { of } from 'rxjs';
            import { catchError, tap } from 'rxjs/operators';
            ${MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_IMPORT_STATEMENT}
            import { MatButtonModule } from '@angular/material/button';
            import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
            import { MessageDetails, MessagePanelComponent } from '@maia/maia-ui';
            
            
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
