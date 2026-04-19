import {Component, DestroyRef, inject, input, OnInit, output} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatButtonModule} from '@angular/material/button';
import {JobState} from '../../models/JobState';
import {JobExecutionSummary} from '../../models/JobExecutionSummary';
import {JobExecutionState} from '../../models/JobExecutionState';
import {JobsApiService} from '../../services/jobs-api.service';


@Component({
    selector: 'maia-job-state',
    templateUrl: './job-state.component.html',
    imports: [MatButtonModule]
})
export class JobStateComponent implements OnInit {


    jobState = input.required<JobState>();


    runJob = output<JobState>();


    displayStackTrace = output<string>();


    displayJobMetrics = output<JobExecutionState>();


    recentlyFailedJobs: JobExecutionSummary[] = [];


    private destroyRef = inject(DestroyRef);


    constructor(private jobsService: JobsApiService) {}


    ngOnInit() {
        this.jobsService.getRecentlyFailedJobExecutions(this.jobState().jobName)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(data => this.recentlyFailedJobs = data);
    }


    onRun(jobState: JobState) {
        this.runJob.emit(jobState);
    }


    onDisplayStackTrace(jobExecutionId: string) {
        this.displayStackTrace.emit(jobExecutionId);
    }


    onDisplayJobMetrics(jobExecution: JobExecutionState) {
        this.displayJobMetrics.emit(jobExecution);
    }


}
