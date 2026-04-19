import {Component, Inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';


@Component({
    selector: 'maia-stacktrace-dialog',
    templateUrl: './stacktrace-dialog.component.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class StacktraceDialogComponent {


    constructor(
        public dialogRef: MatDialogRef<StacktraceDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public stacktrace: string
    ) {}


    onClose(): void {

        this.dialogRef.close();

    }


}
