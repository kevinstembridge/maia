import {describe, expect, it} from 'vitest';
import {buildIndexCountSummary, countByStatus, filterAndSortByName, filterBySystemIndices} from './elastic-indices-filtering';
import {EsIndexStateDto} from '../models/EsIndexStateDto';


function indexDto(overrides: Partial<EsIndexStateDto> & {indexName: string}): EsIndexStateDto {
    return {
        indexExists: true,
        summary: {indexName: overrides.indexName, description: '', isActiveVersion: false},
        health: {indexName: overrides.indexName, status: 'green'},
        ...overrides
    } as EsIndexStateDto;
}


describe('elastic-indices-filtering', () => {


    describe('filterBySystemIndices()', () => {

        it('returns all indices when hideSystemIndices is false', () => {
            const indices = [indexDto({indexName: '.security'}), indexDto({indexName: 'user-events'})];
            expect(filterBySystemIndices(indices, false)).toEqual(indices);
        });

        it('excludes indices starting with "." when hideSystemIndices is true', () => {
            const visible = indexDto({indexName: 'user-events'});
            const indices = [indexDto({indexName: '.security'}), visible];
            expect(filterBySystemIndices(indices, true)).toEqual([visible]);
        });

    });


    describe('filterAndSortByName()', () => {

        it('returns all indices sorted alphabetically when nameFilter is empty', () => {
            const indices = [indexDto({indexName: 'zeta'}), indexDto({indexName: 'alpha'})];
            expect(filterAndSortByName(indices, '').map((it) => it.indexName)).toEqual(['alpha', 'zeta']);
        });

        it('matches indexName case-insensitively', () => {
            const indices = [indexDto({indexName: 'User-Events'}), indexDto({indexName: 'audit-log'})];
            expect(filterAndSortByName(indices, 'user').map((it) => it.indexName)).toEqual(['User-Events']);
        });

        it('excludes indices whose name does not contain the filter', () => {
            const indices = [indexDto({indexName: 'user-events'}), indexDto({indexName: 'audit-log'})];
            expect(filterAndSortByName(indices, 'zzz')).toEqual([]);
        });

    });


    describe('countByStatus()', () => {

        it('groups indices by lowercased health status', () => {
            const indices = [
                indexDto({indexName: 'a', health: {indexName: 'a', status: 'GREEN'}}),
                indexDto({indexName: 'b', health: {indexName: 'b', status: 'green'}}),
                indexDto({indexName: 'c', health: {indexName: 'c', status: 'red'}})
            ];
            expect(countByStatus(indices)).toEqual({green: 2, red: 1});
        });

        it('ignores indices with no health status', () => {
            const indices = [indexDto({indexName: 'a', health: undefined as unknown as EsIndexStateDto['health']})];
            expect(countByStatus(indices)).toEqual({});
        });

    });


    describe('buildIndexCountSummary()', () => {

        it('shows a plain count with ordered status segments when the filter does not narrow the set', () => {
            expect(buildIndexCountSummary(12, 12, {yellow: 3, green: 8, red: 1}))
                .toEqual('12 indices · 8 green · 3 yellow · 1 red');
        });

        it('shows "X of Y" when the filter narrows the set', () => {
            expect(buildIndexCountSummary(3, 12, {green: 2, red: 1}))
                .toEqual('3 of 12 indices · 2 green · 1 red');
        });

        it('omits status segments with a zero count', () => {
            expect(buildIndexCountSummary(5, 5, {green: 5, yellow: 0}))
                .toEqual('5 indices · 5 green');
        });

        it('orders unknown statuses alphabetically after green/yellow/red', () => {
            expect(buildIndexCountSummary(3, 3, {red: 1, unknown: 1, aqua: 1}))
                .toEqual('3 indices · 1 red · 1 aqua · 1 unknown');
        });

        it('uses singular "index" when the total is 1', () => {
            expect(buildIndexCountSummary(1, 1, {green: 1})).toEqual('1 index · 1 green');
        });

        it('shows just the count label when there are no status counts', () => {
            expect(buildIndexCountSummary(0, 0, {})).toEqual('0 indices');
        });

    });


});
