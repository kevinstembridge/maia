import {describe, expect, it} from 'vitest';
import {countByDisplayStatus, deriveDisplayStatus, filterAndSortByName, filterBySystemIndices, filterByStatus} from './elastic-indices-filtering';
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


    describe('deriveDisplayStatus()', () => {

        it('returns "not-created" when the index does not exist, even if health data is present', () => {
            const index = indexDto({indexName: 'a', indexExists: false, health: {indexName: 'a', status: 'green'}});
            expect(deriveDisplayStatus(index)).toEqual('not-created');
        });

        it('returns the lowercased health status for an existing index', () => {
            const index = indexDto({indexName: 'a', indexExists: true, health: {indexName: 'a', status: 'YELLOW'}});
            expect(deriveDisplayStatus(index)).toEqual('yellow');
        });

        it('returns undefined for an existing index with no health status', () => {
            const index = indexDto({indexName: 'a', indexExists: true, health: undefined as unknown as EsIndexStateDto['health']});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

        it('returns undefined for an existing index with an unrecognized health status', () => {
            const index = indexDto({indexName: 'a', indexExists: true, health: {indexName: 'a', status: 'purple'}});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

    });


    describe('countByDisplayStatus()', () => {

        it('counts every display status, including zeros for statuses with no matches', () => {
            const indices = [
                indexDto({indexName: 'a', health: {indexName: 'a', status: 'GREEN'}}),
                indexDto({indexName: 'b', health: {indexName: 'b', status: 'green'}}),
                indexDto({indexName: 'c', health: {indexName: 'c', status: 'red'}}),
                indexDto({indexName: 'd', indexExists: false})
            ];
            expect(countByDisplayStatus(indices)).toEqual({green: 2, yellow: 0, red: 1, 'not-created': 1});
        });

        it('returns all zeros for an empty list', () => {
            expect(countByDisplayStatus([])).toEqual({green: 0, yellow: 0, red: 0, 'not-created': 0});
        });

    });


    describe('filterByStatus()', () => {

        it('returns all indices unchanged when status is null', () => {
            const indices = [indexDto({indexName: 'a'}), indexDto({indexName: 'b', indexExists: false})];
            expect(filterByStatus(indices, null)).toEqual(indices);
        });

        it('returns only indices matching the given status', () => {
            const green = indexDto({indexName: 'a', health: {indexName: 'a', status: 'green'}});
            const red = indexDto({indexName: 'b', health: {indexName: 'b', status: 'red'}});
            expect(filterByStatus([green, red], 'red')).toEqual([red]);
        });

        it('matches "not-created" against indices that do not exist', () => {
            const missing = indexDto({indexName: 'a', indexExists: false});
            const existing = indexDto({indexName: 'b'});
            expect(filterByStatus([missing, existing], 'not-created')).toEqual([missing]);
        });

    });


});
