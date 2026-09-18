import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

export type HistoryStatus = 'running' | 'success' | 'failed';

export type HistoryStatusFilter = 'RUNNING' | 'SUCCESS' | 'FAILED' | null;


export function deriveHistoryStatus(item: JobExecutionHistoryItem): HistoryStatus {
    if (item.completionStatus === null) {
        return 'running';
    }
    return item.completionStatus === 'SUCCESS' ? 'success' : 'failed';
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
