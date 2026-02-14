package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDeleteApiDef

class EntityDeleteDialogHtmlRenderer(private val deleteApiDef: EntityDeleteApiDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return this.deleteApiDef.dialogHtmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("""
            |<h1 mat-dialog-title>Delete?</h1>
            |<div mat-dialog-content>
            |    @if (problemDetail()) {
            |        <mat-error>
            |            <p class="alert alert-warning">{{ problemDetail()!.title }}</p>
            |        </mat-error>
            |    }
            |    <h5>Do you want to delete this record?</h5>
            |    <h5>    {{ message }}</h5>
            |</div>
            |<div mat-dialog-actions>
            |    <button mat-flat-button (click)="onYes()" color="primary" cdkFocusInitial>Yes</button>
            |    <button mat-flat-button (click)="onCancel()">Cancel</button>
            |</div>""".trimMargin())

        return sourceCode.toString()

    }


}
