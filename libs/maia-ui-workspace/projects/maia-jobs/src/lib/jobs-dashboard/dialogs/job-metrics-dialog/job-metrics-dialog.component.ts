import {Component, Inject, signal, WritableSignal} from '@angular/core';
import {JsonPipe} from '@angular/common';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {JobMetricsReport} from '../../models/JobMetricsReport';
import {JobMetricsNodeComponent} from './components/job-metrics-node/job-metrics-node.component';

export interface JobMetricsDialogData {
    metrics: JobMetricsReport;
    // When provided, a refresh button is shown; emits the latest metrics, or undefined if unavailable.
    refresh?: () => Observable<JobMetricsReport | undefined>;
}

@Component({
    selector: 'maia-job-metrics-dialog',
    templateUrl: './job-metrics-dialog.component.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule, ClipboardModule, MatIconModule, JsonPipe, JobMetricsNodeComponent]
})
export class JobMetricsDialogComponent {


    metrics: WritableSignal<JobMetricsReport>;

    refreshing = signal(false);

    canRefresh: boolean;


    constructor(
        public dialogRef: MatDialogRef<JobMetricsDialogComponent>,
        @Inject(MAT_DIALOG_DATA) private data: JobMetricsDialogData
    ) {

        this.metrics = signal(data.metrics);
        this.canRefresh = data.refresh !== undefined;

    }


    onRefresh(): void {

        if (!this.data.refresh || this.refreshing()) {
            return;
        }

        this.refreshing.set(true);

        this.data.refresh().subscribe({
            next: latest => {
                if (latest) {
                    this.metrics.set(latest);
                }
                this.refreshing.set(false);
            },
            error: err => {
                console.error(err);
                this.refreshing.set(false);
            }
        });

    }


    onClose(): void {

        this.dialogRef.close();

    }


}
