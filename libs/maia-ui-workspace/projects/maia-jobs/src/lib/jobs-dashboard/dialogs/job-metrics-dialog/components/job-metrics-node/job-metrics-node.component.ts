import {Component, computed, input, signal} from '@angular/core';
import {KeyValuePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {JobMetricsReport} from '../../../../models/JobMetricsReport';

@Component({
    selector: 'maia-job-metrics-node',
    templateUrl: './job-metrics-node.component.html',
    styleUrl: './job-metrics-node.component.scss',
    imports: [KeyValuePipe, MatButtonModule, MatIconModule, JobMetricsNodeComponent]
})
export class JobMetricsNodeComponent {


    report = input.required<JobMetricsReport>();

    expanded = signal(true);


    hasChildJobs = computed(() => (this.report().childJobs?.length ?? 0) > 0);

    hasTimingStats = computed(() => this.report().min !== undefined);

    hasContext = computed(() => Object.keys(this.report().context ?? {}).length > 0);

    hasCounters = computed(() => Object.keys(this.report().counters ?? {}).length > 0);

    hasRatios = computed(() => Object.keys(this.report().ratios ?? {}).length > 0);


    onToggleExpanded(): void {

        this.expanded.update(value => !value);

    }


    formatSeconds(seconds: number | undefined): string {

        if (seconds === undefined) {
            return '';
        }

        return `${seconds.toFixed(3)}s`;

    }


    formatElapsedTime(seconds: number): string {

        const totalSeconds = Math.round(seconds);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const secs = totalSeconds % 60;

        if (hours > 0) {
            return `${hours}h ${minutes}m ${secs}s`;
        }

        if (minutes > 0) {
            return `${minutes}m ${secs}s`;
        }

        return `${secs}s`;

    }


}
