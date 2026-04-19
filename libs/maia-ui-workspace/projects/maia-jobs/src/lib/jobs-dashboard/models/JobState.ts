import {JobExecutionState} from './JobExecutionState';

export class JobState {
    jobName!: string;
    jobDescription!: string;
    runningJobs!: JobExecutionState[];
}
