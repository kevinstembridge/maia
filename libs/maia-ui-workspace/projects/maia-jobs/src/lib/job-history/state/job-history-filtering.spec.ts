import {describe, expect, it} from 'vitest';
import {convertToParamMap} from '@angular/router';
import {DateTime} from 'luxon';
import {
    buildHistoryFilterQueryParams,
    deriveHistoryStatus,
    formatDuration,
    parseHistoryFiltersFromParams,
    toEndOfDayIso,
    toStartOfDayIso
} from './job-history-filtering';
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

describe('toStartOfDayIso()', () => {

    it('returns null when date is null', () => {
        expect(toStartOfDayIso(null)).toBeNull();
    });

    it('returns the UTC start-of-day ISO string for the given date', () => {
        const date = DateTime.fromISO('2026-09-10T15:30:00', {zone: 'utc'});
        expect(toStartOfDayIso(date)).toEqual('2026-09-10T00:00:00.000Z');
    });

});

describe('toEndOfDayIso()', () => {

    it('returns null when date is null', () => {
        expect(toEndOfDayIso(null)).toBeNull();
    });

    it('returns the UTC end-of-day ISO string for the given date', () => {
        const date = DateTime.fromISO('2026-09-10T15:30:00', {zone: 'utc'});
        expect(toEndOfDayIso(date)).toEqual('2026-09-10T23:59:59.999Z');
    });

});

describe('parseHistoryFiltersFromParams()', () => {

    it('parses all four filters when all params are present', () => {
        const params = convertToParamMap({jobName: 'nightly-job', status: 'FAILED', from: '2026-09-01', to: '2026-09-10'});
        expect(parseHistoryFiltersFromParams(params)).toEqual({
            jobNameFilter: 'nightly-job',
            statusFilter: 'FAILED',
            fromDate: '2026-09-01T00:00:00.000Z',
            toDate: '2026-09-10T23:59:59.999Z',
        });
    });

    it('returns all-null filters when no params are present', () => {
        expect(parseHistoryFiltersFromParams(convertToParamMap({}))).toEqual({
            jobNameFilter: null,
            statusFilter: null,
            fromDate: null,
            toDate: null,
        });
    });

    it('falls back to a null statusFilter for an invalid status value', () => {
        const params = convertToParamMap({status: 'BOGUS'});
        expect(parseHistoryFiltersFromParams(params).statusFilter).toBeNull();
    });

});

describe('buildHistoryFilterQueryParams()', () => {

    it('round-trips a fully-populated filter set back to its URL param form', () => {
        const params = convertToParamMap({jobName: 'nightly-job', status: 'FAILED', from: '2026-09-01', to: '2026-09-10'});
        const filters = parseHistoryFiltersFromParams(params);
        expect(buildHistoryFilterQueryParams(filters)).toEqual({
            jobName: 'nightly-job',
            status: 'FAILED',
            from: '2026-09-01',
            to: '2026-09-10',
        });
    });

    it('sets every param to null when all filters are null', () => {
        expect(buildHistoryFilterQueryParams({jobNameFilter: null, statusFilter: null, fromDate: null, toDate: null}))
            .toEqual({jobName: null, status: null, from: null, to: null});
    });

});
