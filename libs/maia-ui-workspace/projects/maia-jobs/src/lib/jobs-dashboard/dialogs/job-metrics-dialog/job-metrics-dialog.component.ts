import {Component, Inject} from '@angular/core';
import {JsonPipe} from '@angular/common';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';

@Component({
    selector: 'maia-job-metrics-dialog',
    templateUrl: './job-metrics-dialog.component.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule, ClipboardModule, MatIconModule, JsonPipe]
})
export class JobMetricsDialogComponent {


    constructor(
        public dialogRef: MatDialogRef<JobMetricsDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public metrics: any
    ) {}


    onClose(): void {

        this.dialogRef.close();

    }


}
