import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {JobState} from './models/JobState';
import {JobExecutionState} from './models/JobExecutionState';
import {JobsApiService} from './services/jobs-api.service';
import {JobsDashboardStore} from './state/jobs-dashboard-store';
import {JobStateComponent} from './components/job-state/job-state.component';
import {JobMetricsDialogComponent} from './dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {RunJobDialogComponent} from './dialogs/run-job-dialog/run-job-dialog.component';
import {StacktraceDialogComponent} from './dialogs/stacktrace-dialog/stacktrace-dialog.component';


@Component({
    imports: [JobStateComponent, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule, RouterLink],
    providers: [JobsApiService, JobsDashboardStore],
    selector: 'maia-jobs-dashboard-page',
    templateUrl: './jobs-dashboard-page.component.html',
    styleUrl: './jobs-dashboard-page.component.scss'
})
export class JobsDashboardPageComponent implements OnInit {


    readonly store = inject(JobsDashboardStore);


    constructor(
        private jobsService: JobsApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {
        this.store.startPolling();
    }


    onFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onRunJob(jobState: JobState) {

      const dialogRef = this.dialog.open(RunJobDialogComponent, {data: jobState});
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.jobsService.runJob(jobState.jobName);
            }
        });

    }


    onDisplayStackTrace(jobExecutionId: string) {

        this.jobsService.getStacktrace(jobExecutionId).subscribe(res => {
            this.dialog.open(StacktraceDialogComponent, {data: res.stacktrace});
        });

    }


    onDisplayJobMetricsDialog(jobExecutionState: JobExecutionState) {

        this.dialog.open(JobMetricsDialogComponent, {data: jobExecutionState.metrics, width: '90vw', maxWidth: '90vw'});

    }


}
