import {Component, computed, input, output, signal} from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {JobStateResponseDto} from '@app/gen-components/org/maiaframework/job/JobStateResponseDto';
import {RunningJobStateResponseDto} from '@app/gen-components/org/maiaframework/job/RunningJobStateResponseDto';
import {deriveJobStatus, formatElapsed, JobStatus} from '../../state/jobs-filtering';

const STATUS_COLORS: Record<JobStatus, string> = {
    running: '#2196f3',
    failed: '#d32f2f',
    idle: '#4caf50',
};

const STATUS_LABELS: Record<JobStatus, string> = {
    running: 'Running',
    failed: 'Failed',
    idle: 'Idle',
};

@Component({
    selector: 'maia-job-state',
    templateUrl: './job-state.component.html',
    styleUrl: './job-state.component.scss',
    imports: [MatButtonModule, MatIconModule, RouterLink]
})
export class JobStateComponent {


    jobState = input.required<JobStateResponseDto>();

    now = input.required<number>();


    runJob = output<JobStateResponseDto>();

    displayStackTrace = output<string>();

    displayJobMetrics = output<RunningJobStateResponseDto>();


    failuresExpanded = signal(false);


    status = computed<JobStatus>(() => deriveJobStatus(this.jobState()));

    statusColor = computed<string>(() => STATUS_COLORS[this.status()]);

    statusLabel = computed<string>(() => STATUS_LABELS[this.status()]);

    runningRows = computed(() =>
        this.jobState().runningJobs.map((execution) => ({
            execution,
            elapsedLabel: formatElapsed(execution.startTimestamp, this.now())
        }))
    );


    onRun() {
        this.runJob.emit(this.jobState());
    }


    onDisplayStackTrace(jobExecutionId: string) {
        this.displayStackTrace.emit(jobExecutionId);
    }


    onDisplayJobMetrics(jobExecution: RunningJobStateResponseDto) {
        this.displayJobMetrics.emit(jobExecution);
    }


    onToggleFailures() {
        this.failuresExpanded.set(!this.failuresExpanded());
    }


}
