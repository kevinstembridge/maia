export class JobExecutionHistoryItem {
    jobExecutionId!: string;
    jobName!: string;
    invokedBy!: string;
    startTimestamp!: string;
    endTimestamp!: string | null;
    status!: 'RUNNING' | 'SUCCESS' | 'FAILED';
    errorMessage!: string | null;
    metrics!: any;
}
