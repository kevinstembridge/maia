import {ParamMap, Params} from '@angular/router';
import {JobState} from '../models/JobState';

export type JobStatus = 'running' | 'failed' | 'idle';

export const STATUS_DISPLAY_ORDER: JobStatus[] = ['running', 'failed', 'idle'];

export const STATUS_COLORS: Record<JobStatus, string> = {
    running: '#1976d2',
    failed: '#d32f2f',
    idle: '#9e9e9e',
};


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


export function countByStatus(jobs: JobState[]): Record<JobStatus, number> {
    const counts: Record<JobStatus, number> = {running: 0, failed: 0, idle: 0};
    for (const job of jobs) {
        counts[deriveJobStatus(job)]++;
    }
    return counts;
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


export function parseStatusFilterFromParams(params: ParamMap): JobStatus | null {
    const status = params.get('status');
    return STATUS_DISPLAY_ORDER.includes(status as JobStatus) ? (status as JobStatus) : null;
}


export function buildStatusFilterQueryParams(statusFilter: JobStatus | null): Params {
    return {status: statusFilter};
}
