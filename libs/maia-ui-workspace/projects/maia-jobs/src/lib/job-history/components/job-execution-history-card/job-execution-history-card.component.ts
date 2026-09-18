import {Component, computed, input, output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {JobExecutionHistoryItem} from '../../../jobs-dashboard/models/JobExecutionHistoryItem';
import {deriveHistoryStatus, formatDuration, HistoryStatus} from '../../state/job-history-filtering';

const STATUS_COLORS: Record<HistoryStatus, string> = {
    running: '#2196f3',
    success: '#4caf50',
    failed: '#d32f2f',
};

const STATUS_LABELS: Record<HistoryStatus, string> = {
    running: 'Running',
    success: 'Success',
    failed: 'Failed',
};

@Component({
    selector: 'maia-job-execution-history-card',
    templateUrl: './job-execution-history-card.component.html',
    styleUrl: './job-execution-history-card.component.scss',
    imports: [MatButtonModule, DatePipe]
})
export class JobExecutionHistoryCardComponent {


    historyItem = input.required<JobExecutionHistoryItem>();


    displayStackTrace = output<string>();

    displayJobMetrics = output<any>();


    status = computed<HistoryStatus>(() => deriveHistoryStatus(this.historyItem()));

    statusColor = computed<string>(() => STATUS_COLORS[this.status()]);

    statusLabel = computed<string>(() => STATUS_LABELS[this.status()]);

    durationLabel = computed<string>(() =>
        formatDuration(this.historyItem().startTimestamp, this.historyItem().endTimestamp)
    );


    onDisplayStackTrace() {
        this.displayStackTrace.emit(this.historyItem().jobExecutionId);
    }


    onDisplayJobMetrics() {
        this.displayJobMetrics.emit(this.historyItem().metrics);
    }


}
