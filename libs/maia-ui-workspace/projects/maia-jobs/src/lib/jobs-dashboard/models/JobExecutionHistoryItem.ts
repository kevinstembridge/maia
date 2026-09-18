export class JobExecutionHistoryItem {
    jobExecutionId!: string;
    jobName!: string;
    invokedBy!: string;
    startTimestamp!: string;
    endTimestamp!: string | null;
    completionStatus!: 'SUCCESS' | 'FAILED' | null;
    errorMessage!: string | null;
    metrics!: any;
}
