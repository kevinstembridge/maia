import {Component, Inject, ChangeDetectionStrategy} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {JobStateResponseDto} from '@app/gen-components/org/maiaframework/job/JobStateResponseDto';

@Component({
    selector: 'maia-run-job-dialog',
    templateUrl: './run-job-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class RunJobDialogComponent {


    constructor(
        public dialogRef: MatDialogRef<RunJobDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public jobState: JobStateResponseDto
    ) {}


    onSubmit() {

      this.dialogRef.close(true);

    }


    onCancel(): void {

        this.dialogRef.close();

    }


}
