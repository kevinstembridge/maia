import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

export type HistoryStatus = 'running' | 'success' | 'failed';

export type HistoryStatusFilter = 'RUNNING' | 'SUCCESS' | 'FAILED' | null;


const STATUS_MAP: Record<JobExecutionHistoryItem['status'], HistoryStatus> = {
    RUNNING: 'running',
    SUCCESS: 'success',
    FAILED: 'failed',
};

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
