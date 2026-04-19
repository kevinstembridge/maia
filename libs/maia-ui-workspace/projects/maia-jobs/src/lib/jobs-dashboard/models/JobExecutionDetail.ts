export class JobExecutionDetail {
    jobExecutionId!: string;
    jobName!: string;
    startTimestampUtc!: string;
    endTimestampUtc!: string;
    errorMessage!: string;
    stackTrace!: string;
}
