import {Component, Inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {JobState} from '../../models/JobState';

@Component({
    selector: 'maia-run-job-dialog',
    templateUrl: './run-job-dialog.component.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class RunJobDialogComponent {


    constructor(
        public dialogRef: MatDialogRef<RunJobDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public jobState: JobState
    ) {}


    onSubmit() {

      this.dialogRef.close(true);

    }


    onCancel(): void {

        this.dialogRef.close();

    }


}
