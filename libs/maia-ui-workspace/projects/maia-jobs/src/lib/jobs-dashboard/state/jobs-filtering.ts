import {ParamMap, Params} from '@angular/router';
import {JobState} from '../models/JobState';

export type JobStatus = 'running' | 'failed' | 'idle';

const STATUS_DISPLAY_ORDER: JobStatus[] = ['running', 'failed', 'idle'];


export function filterAndSortByName(jobs: JobState[], nameFilter: string): JobState[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return jobs
        .filter((it) => it.jobName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.jobName.localeCompare(b.jobName));
}


export function deriveJobStatus(jobState: JobState): JobStatus {
    if (jobState.runningJobs.length > 0) {
        return 'running';
    }
    if (jobState.recentlyFailedExecutions.length > 0) {
        return 'failed';
    }
    return 'idle';
}


export function countByStatus(jobs: JobState[]): Record<string, number> {
    const counts: Record<string, number> = {};
    for (const job of jobs) {
        const status = deriveJobStatus(job);
        counts[status] = (counts[status] ?? 0) + 1;
    }
    return counts;
}


export function buildJobCountSummary(
    visibleCount: number,
    totalCount: number,
    statusCounts: Record<string, number>
): string {
    const jobWord = totalCount === 1 ? 'job' : 'jobs';
    const countLabel = visibleCount === totalCount
        ? `${totalCount} ${jobWord}`
        : `${visibleCount} of ${totalCount} ${jobWord}`;

    const statusSegments = STATUS_DISPLAY_ORDER
        .filter((status) => (statusCounts[status] ?? 0) > 0)
        .map((status) => `${statusCounts[status]} ${status}`);

    return statusSegments.length === 0
        ? countLabel
        : `${countLabel} · ${statusSegments.join(' · ')}`;
}


export function formatElapsed(startTimestamp: string, now: number): string {
    const elapsedMs = Math.max(0, now - new Date(startTimestamp).getTime());
    const totalSeconds = Math.floor(elapsedMs / 1000);

    if (totalSeconds < 60) {
        return `${totalSeconds}s`;
    }

    if (totalSeconds < 3600) {
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}m ${seconds}s`;
    }

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    return `${hours}h ${String(minutes).padStart(2, '0')}m`;
}


export function parseNameFilterFromParams(params: ParamMap): string {
    return params.get('jobName') ?? '';
}


export function buildNameFilterQueryParams(nameFilter: string): Params {
    return {jobName: nameFilter.length > 0 ? nameFilter : null};
}
