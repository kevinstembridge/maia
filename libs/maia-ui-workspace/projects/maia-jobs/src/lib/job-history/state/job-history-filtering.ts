import {DateTime} from 'luxon';
import {ParamMap, Params} from '@angular/router';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

export type HistoryStatus = 'running' | 'success' | 'failed';

export type HistoryStatusFilter = 'RUNNING' | 'SUCCESS' | 'FAILED' | null;

export interface HistoryFilters {
    jobNameFilter: string | null;
    statusFilter: HistoryStatusFilter;
    fromDate: string | null;
    toDate: string | null;
}

const STATUS_MAP: Record<JobExecutionHistoryItem['status'], HistoryStatus> = {
    RUNNING: 'running',
    SUCCESS: 'success',
    FAILED: 'failed',
};

const VALID_STATUS_FILTERS: string[] = ['RUNNING', 'SUCCESS', 'FAILED'];


export function deriveHistoryStatus(item: JobExecutionHistoryItem): HistoryStatus {
    return STATUS_MAP[item.status];
}


export function formatDuration(startTimestamp: string, endTimestamp: string | null): string {

    if (endTimestamp === null) {
        return 'Running…';
    }

    const elapsedMs = Math.max(0, new Date(endTimestamp).getTime() - new Date(startTimestamp).getTime());
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


export function toStartOfDayIso(date: DateTime | null): string | null {

    if (!date) {
        return null;
    }

    return date.startOf('day').toUTC().toISO();

}


export function toEndOfDayIso(date: DateTime | null): string | null {

    if (!date) {
        return null;
    }

    return date.endOf('day').toUTC().toISO();

}


export function parseHistoryFiltersFromParams(params: ParamMap): HistoryFilters {

    const rawStatus = params.get('status');
    const statusFilter = rawStatus !== null && VALID_STATUS_FILTERS.includes(rawStatus)
        ? rawStatus as HistoryStatusFilter
        : null;

    const from = params.get('from');
    const to = params.get('to');

    return {
        jobNameFilter: params.get('jobName'),
        statusFilter,
        fromDate: toStartOfDayIso(from ? DateTime.fromISO(from, {zone: 'utc'}) : null),
        toDate: toEndOfDayIso(to ? DateTime.fromISO(to, {zone: 'utc'}) : null),
    };

}


export function buildHistoryFilterQueryParams(filters: HistoryFilters): Params {
    return {
        jobName: filters.jobNameFilter,
        status: filters.statusFilter,
        from: filters.fromDate ? DateTime.fromISO(filters.fromDate, {zone: 'utc'}).toISODate() : null,
        to: filters.toDate ? DateTime.fromISO(filters.toDate, {zone: 'utc'}).toISODate() : null,
    };
}
