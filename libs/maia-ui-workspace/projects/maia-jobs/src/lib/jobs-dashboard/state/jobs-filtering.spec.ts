import {describe, expect, it} from 'vitest';
import {
    buildJobCountSummary,
    countByStatus,
    deriveJobStatus,
    filterAndSortByName,
    formatElapsed
} from './jobs-filtering';
import {JobState} from '../models/JobState';
import {JobExecutionState} from '../models/JobExecutionState';
import {JobExecutionSummary} from '../models/JobExecutionSummary';


function runningExecution(overrides: Partial<JobExecutionState> = {}): JobExecutionState {
    return {
        id: 'exec-1',
        jobName: 'some-job',
        invokedBy: 'someone',
        startTimestamp: '2026-01-01T00:00:00.000Z',
        metrics: {},
        ...overrides
    } as JobExecutionState;
}


function failedExecution(overrides: Partial<JobExecutionSummary> = {}): JobExecutionSummary {
    return {
        jobExecutionId: 'exec-failed-1',
        jobName: 'some-job',
        startTimestamp: '2026-01-01T00:00:00.000Z',
        endTimestamp: '2026-01-01T00:05:00.000Z',
        errorMessage: 'boom',
        ...overrides
    } as JobExecutionSummary;
}


function jobState(overrides: Partial<JobState> & {jobName: string}): JobState {
    return {
        jobDescription: '',
        runningJobs: [],
        recentlyFailedExecutions: [],
        ...overrides
    } as JobState;
}


describe('jobs-filtering', () => {


    describe('filterAndSortByName()', () => {

        it('returns all jobs sorted alphabetically when nameFilter is empty', () => {
            const jobs = [jobState({jobName: 'zeta-job'}), jobState({jobName: 'alpha-job'})];
            expect(filterAndSortByName(jobs, '').map((it) => it.jobName)).toEqual(['alpha-job', 'zeta-job']);
        });

        it('matches jobName case-insensitively', () => {
            const jobs = [jobState({jobName: 'Nightly-Reconciliation'}), jobState({jobName: 'weekly-export'})];
            expect(filterAndSortByName(jobs, 'nightly').map((it) => it.jobName)).toEqual(['Nightly-Reconciliation']);
        });

        it('excludes jobs whose name does not contain the filter', () => {
            const jobs = [jobState({jobName: 'nightly-reconciliation'}), jobState({jobName: 'weekly-export'})];
            expect(filterAndSortByName(jobs, 'zzz')).toEqual([]);
        });

    });


    describe('deriveJobStatus()', () => {

        it('returns "idle" when there are no running jobs and no recent failures', () => {
            expect(deriveJobStatus(jobState({jobName: 'a'}))).toEqual('idle');
        });

        it('returns "failed" when there are recent failures and nothing running', () => {
            expect(deriveJobStatus(jobState({jobName: 'a', recentlyFailedExecutions: [failedExecution()]}))).toEqual('failed');
        });

        it('returns "running" when there are running executions', () => {
            expect(deriveJobStatus(jobState({jobName: 'a', runningJobs: [runningExecution()]}))).toEqual('running');
        });

        it('returns "running" (not "failed") when both running executions and recent failures are present', () => {
            const state = jobState({
                jobName: 'a',
                runningJobs: [runningExecution()],
                recentlyFailedExecutions: [failedExecution()]
            });
            expect(deriveJobStatus(state)).toEqual('running');
        });

    });


    describe('countByStatus()', () => {

        it('partitions jobs into running/failed/idle buckets using deriveJobStatus priority', () => {
            const jobs = [
                jobState({jobName: 'a', runningJobs: [runningExecution()]}),
                jobState({jobName: 'b', recentlyFailedExecutions: [failedExecution()]}),
                jobState({jobName: 'c'}),
                jobState({jobName: 'd', runningJobs: [runningExecution()], recentlyFailedExecutions: [failedExecution()]})
            ];
            expect(countByStatus(jobs)).toEqual({running: 2, failed: 1, idle: 1});
        });

    });


    describe('buildJobCountSummary()', () => {

        it('shows a plain count with ordered status segments when the filter does not narrow the set', () => {
            expect(buildJobCountSummary(8, 8, {running: 2, failed: 1, idle: 5}))
                .toEqual('8 jobs · 2 running · 1 failed · 5 idle');
        });

        it('shows "X of Y" when the filter narrows the set', () => {
            expect(buildJobCountSummary(3, 8, {running: 2, idle: 1}))
                .toEqual('3 of 8 jobs · 2 running · 1 idle');
        });

        it('omits status segments with a zero count', () => {
            expect(buildJobCountSummary(5, 5, {idle: 5, running: 0}))
                .toEqual('5 jobs · 5 idle');
        });

        it('uses singular "job" when the total is 1', () => {
            expect(buildJobCountSummary(1, 1, {idle: 1})).toEqual('1 job · 1 idle');
        });

        it('shows just the count label when there are no status counts', () => {
            expect(buildJobCountSummary(0, 0, {})).toEqual('0 jobs');
        });

    });


    describe('formatElapsed()', () => {

        it('formats durations under a minute as seconds', () => {
            const start = '2026-01-01T00:00:00.000Z';
            const now = new Date('2026-01-01T00:00:42.000Z').getTime();
            expect(formatElapsed(start, now)).toEqual('42s');
        });

        it('formats durations under an hour as minutes and seconds', () => {
            const start = '2026-01-01T00:00:00.000Z';
            const now = new Date('2026-01-01T00:04:12.000Z').getTime();
            expect(formatElapsed(start, now)).toEqual('4m 12s');
        });

        it('formats durations of an hour or more as hours and zero-padded minutes', () => {
            const start = '2026-01-01T00:00:00.000Z';
            const now = new Date('2026-01-01T01:03:00.000Z').getTime();
            expect(formatElapsed(start, now)).toEqual('1h 03m');
        });

    });


});
