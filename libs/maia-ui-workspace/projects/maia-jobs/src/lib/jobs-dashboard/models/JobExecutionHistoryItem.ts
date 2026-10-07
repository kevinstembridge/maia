export class JobExecutionHistoryItem {
    jobExecutionId!: string;
    jobName!: string;
    invokedBy!: string;
    startTimestamp!: string;
    endTimestamp!: string | null;
    status!: 'RUNNING' | 'SUCCESS' | 'FAILED' | 'ABANDONED';
    errorMessage!: string | null;
    metrics!: any;
}
