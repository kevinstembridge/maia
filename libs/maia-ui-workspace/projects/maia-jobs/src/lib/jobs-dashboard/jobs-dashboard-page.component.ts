import {Component, OnInit} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {JobState} from './models/JobState';
import {JobExecutionState} from './models/JobExecutionState';
import {JobsApiService} from './services/jobs-api.service';
import {JobStateComponent} from './components/job-state/job-state.component';
import {JobMetricsDialogComponent} from './dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {RunJobDialogComponent} from './dialogs/run-job-dialog/run-job-dialog.component';
import {StacktraceDialogComponent} from './dialogs/stacktrace-dialog/stacktrace-dialog.component';


@Component({
    imports: [JobStateComponent, MatProgressSpinner, AsyncPipe],
    providers: [JobsApiService],
    selector: 'maia-jobs-dashboard-page',
    templateUrl: './jobs-dashboard-page.component.html'
})
export class JobsDashboardPageComponent implements OnInit {


    jobs$!: Observable<JobState[]>;


    constructor(
        private jobsService: JobsApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {

        this.jobs$ = this.jobsService.getJobsState();

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

        this.dialog.open(JobMetricsDialogComponent, {data: jobExecutionState.metrics});

    }


}
