import {Component, effect, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {JobState} from './models/JobState';
import {JobExecutionState} from './models/JobExecutionState';
import {JobsApiService} from './services/jobs-api.service';
import {JobsDashboardStore} from './state/jobs-dashboard-store';
import {
    buildNameFilterQueryParams,
    buildStatusFilterQueryParams,
    JobStatus,
    parseNameFilterFromParams,
    parseStatusFilterFromParams,
    STATUS_COLORS,
    STATUS_DISPLAY_ORDER
} from './state/jobs-filtering';

const STATUS_LABELS: Record<JobStatus, string> = {
    running: 'Running',
    failed: 'Failed',
    idle: 'Idle',
};
import {JobStateComponent} from './components/job-state/job-state.component';
import {JobMetricsDialogComponent} from './dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {RunJobDialogComponent} from './dialogs/run-job-dialog/run-job-dialog.component';
import {StacktraceDialogComponent} from './dialogs/stacktrace-dialog/stacktrace-dialog.component';


@Component({
    imports: [JobStateComponent, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule, MatIconModule, RouterLink],
    providers: [JobsApiService, JobsDashboardStore],
    selector: 'maia-jobs-dashboard-page',
    templateUrl: './jobs-dashboard-page.component.html',
    styleUrl: './jobs-dashboard-page.component.scss'
})
export class JobsDashboardPageComponent implements OnInit {


    readonly store = inject(JobsDashboardStore);

    readonly statusOrder = STATUS_DISPLAY_ORDER;
    readonly statusColors = STATUS_COLORS;
    readonly statusLabels = STATUS_LABELS;

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private jobsService: JobsApiService,
        private dialog: MatDialog
    ) {

        this.store.onNameFilterChanged(parseNameFilterFromParams(this.route.snapshot.queryParamMap));
        this.store.onStatusFilterChanged(parseStatusFilterFromParams(this.route.snapshot.queryParamMap));

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: {
                    ...buildNameFilterQueryParams(this.store.nameFilter()),
                    ...buildStatusFilterQueryParams(this.store.statusFilter()),
                },
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.startPolling();
    }


    onFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onStatusFilterToggled(status: JobStatus) {
        this.store.onStatusFilterChanged(this.store.statusFilter() === status ? null : status);
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
            this.dialog.open(StacktraceDialogComponent, {data: res.stacktrace, width: '90vw', maxWidth: '90vw'});
        });

    }


    onDisplayJobMetricsDialog(jobExecutionState: JobExecutionState) {

        this.dialog.open(JobMetricsDialogComponent, {data: jobExecutionState.metrics, width: '90vw', maxWidth: '90vw'});

    }


}
