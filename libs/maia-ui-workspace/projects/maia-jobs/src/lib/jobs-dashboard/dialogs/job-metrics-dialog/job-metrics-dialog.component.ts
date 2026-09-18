import {Component, Inject} from '@angular/core';
import {JsonPipe} from '@angular/common';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {JobMetricsReport} from '../../models/JobMetricsReport';
import {JobMetricsNodeComponent} from './components/job-metrics-node/job-metrics-node.component';

@Component({
    selector: 'maia-job-metrics-dialog',
    templateUrl: './job-metrics-dialog.component.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule, ClipboardModule, MatIconModule, JsonPipe, JobMetricsNodeComponent]
})
export class JobMetricsDialogComponent {


    constructor(
        public dialogRef: MatDialogRef<JobMetricsDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public metrics: JobMetricsReport
    ) {}


    onClose(): void {

        this.dialogRef.close();

    }


}
