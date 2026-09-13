import {JobExecutionState} from './JobExecutionState';
import {JobExecutionSummary} from './JobExecutionSummary';

export class JobState {
    jobName!: string;
    description!: string;
    runningJobs!: JobExecutionState[];
    recentlyFailedExecutions!: JobExecutionSummary[];
}
