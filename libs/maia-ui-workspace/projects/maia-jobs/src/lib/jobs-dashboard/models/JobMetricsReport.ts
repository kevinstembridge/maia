export interface JobMetricsReport {
    jobName: string;
    jobCount: number;
    context: Record<string, string>;
    totalElapsedTime: { seconds: number; formatted: string };
    min?: number;
    median?: number;
    '95th'?: number;
    max?: number;
    mean?: number;
    counters?: Record<string, number>;
    ratios?: Record<string, { toString: string; value: number }>;
    childJobs?: JobMetricsReport[];
}
