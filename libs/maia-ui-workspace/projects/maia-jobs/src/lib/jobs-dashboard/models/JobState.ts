import {JobExecutionState} from './JobExecutionState';
import {JobExecutionSummary} from './JobExecutionSummary';

export class JobState {
    jobName!: string;
    jobDescription!: string;
    runningJobs!: JobExecutionState[];
    recentlyFailedExecutions!: JobExecutionSummary[];
}
