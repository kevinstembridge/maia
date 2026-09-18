import {describe, expect, it} from 'vitest';
import {deriveHistoryStatus, formatDuration} from './job-history-filtering';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

function makeItem(overrides: Partial<JobExecutionHistoryItem>): JobExecutionHistoryItem {
    return Object.assign(new JobExecutionHistoryItem(), {
        jobExecutionId: 'exec-1',
        jobName: 'some-job',
        invokedBy: 'someone',
        startTimestamp: '2026-01-01T00:00:00Z',
        endTimestamp: '2026-01-01T00:00:10Z',
        status: 'SUCCESS',
        errorMessage: null,
        metrics: {},
    }, overrides);
}

describe('deriveHistoryStatus', () => {

    it('returns running when status is RUNNING', () => {
        expect(deriveHistoryStatus(makeItem({status: 'RUNNING'}))).toBe('running');
    });

    it('returns success when status is SUCCESS', () => {
        expect(deriveHistoryStatus(makeItem({status: 'SUCCESS'}))).toBe('success');
    });

    it('returns failed when status is FAILED', () => {
        expect(deriveHistoryStatus(makeItem({status: 'FAILED'}))).toBe('failed');
    });

});

describe('formatDuration', () => {

    it('returns Running… when endTimestamp is null', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', null)).toBe('Running…');
    });

    it('formats sub-minute durations as seconds', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T00:00:42Z')).toBe('42s');
    });

    it('formats sub-hour durations as minutes and seconds', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T00:04:12Z')).toBe('4m 12s');
    });

    it('formats hour-plus durations as hours and zero-padded minutes', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T01:03:00Z')).toBe('1h 03m');
    });

});
