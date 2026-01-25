package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDef

class CheckForeignKeyReferencesDialogHtmlRenderer(private val entityDef: EntityDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.entityDef.checkForeignKeyReferencesDialog.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("""
            |<h1 mat-dialog-title>Checking Foreign Key References</h1>
            |<div mat-dialog-content>
            |    @if (checking) {
            |        <mat-spinner></mat-spinner>
            |    }
            |    <app-message-panel [messageDetails]="messageDetails"></app-message-panel>
            |</div>
            |<div mat-dialog-actions>
            |    <button mat-flat-button (click)="onCancel()">Cancel</button>
            |</div>""".trimMargin())

        return sourceCode.toString()

    }


}
