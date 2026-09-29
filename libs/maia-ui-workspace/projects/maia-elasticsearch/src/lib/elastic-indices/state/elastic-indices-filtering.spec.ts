import {describe, expect, it} from 'vitest';
import {convertToParamMap} from '@angular/router';
import {
    buildElasticIndicesQueryParams,
    countByDisplayStatus,
    deriveDisplayStatus,
    filterAndSortByName,
    filterBySystemIndices,
    filterByStatus,
    parseElasticIndicesFiltersFromParams
} from './elastic-indices-filtering';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';


function indexDto(overrides: Partial<IndexStateResponseDto> & {indexName: string}): IndexStateResponseDto {
    return {
        exists: true,
        managedIndexInfo: {
            indexName: {baseName: overrides.indexName, version: 1},
            description: '',
            isActiveVersion: false,
        },
        health: {status: 'green'},
        ...overrides,
    };
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
            const index = indexDto({indexName: 'a', exists: false, health: {status: 'green'}});
            expect(deriveDisplayStatus(index)).toEqual('not-created');
        });

        it('returns the lowercased health status for an existing index', () => {
            const index = indexDto({indexName: 'a', exists: true, health: {status: 'YELLOW'}});
            expect(deriveDisplayStatus(index)).toEqual('yellow');
        });

        it('returns undefined for an existing index with no health status', () => {
            const index = indexDto({indexName: 'a', exists: true, health: undefined});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

        it('returns undefined for an existing index with an unrecognized health status', () => {
            const index = indexDto({indexName: 'a', exists: true, health: {status: 'purple'}});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

    });


    describe('countByDisplayStatus()', () => {

        it('counts every display status, including zeros for statuses with no matches', () => {
            const indices = [
                indexDto({indexName: 'a', health: {status: 'GREEN'}}),
                indexDto({indexName: 'b', health: {status: 'green'}}),
                indexDto({indexName: 'c', health: {status: 'red'}}),
                indexDto({indexName: 'd', exists: false})
            ];
            expect(countByDisplayStatus(indices)).toEqual({green: 2, yellow: 0, red: 1, 'not-created': 1});
        });

        it('returns all zeros for an empty list', () => {
            expect(countByDisplayStatus([])).toEqual({green: 0, yellow: 0, red: 0, 'not-created': 0});
        });

    });


    describe('filterByStatus()', () => {

        it('returns all indices unchanged when status is null', () => {
            const indices = [indexDto({indexName: 'a'}), indexDto({indexName: 'b', exists: false})];
            expect(filterByStatus(indices, null)).toEqual(indices);
        });

        it('returns only indices matching the given status', () => {
            const green = indexDto({indexName: 'a', health: {status: 'green'}});
            const red = indexDto({indexName: 'b', health: {status: 'red'}});
            expect(filterByStatus([green, red], 'red')).toEqual([red]);
        });

        it('matches "not-created" against indices that do not exist', () => {
            const missing = indexDto({indexName: 'a', exists: false});
            const existing = indexDto({indexName: 'b'});
            expect(filterByStatus([missing, existing], 'not-created')).toEqual([missing]);
        });

    });


    describe('parseElasticIndicesFiltersFromParams()', () => {

        it('parses all three filters when all params are present', () => {
            const params = convertToParamMap({indexName: 'audit', status: 'red', hideSystemIndices: 'false'});
            expect(parseElasticIndicesFiltersFromParams(params)).toEqual({
                nameFilter: 'audit',
                statusFilter: 'red',
                hideSystemIndices: false,
            });
        });

        it('defaults hideSystemIndices to true and the rest to empty/null when no params are present', () => {
            expect(parseElasticIndicesFiltersFromParams(convertToParamMap({}))).toEqual({
                nameFilter: '',
                statusFilter: null,
                hideSystemIndices: true,
            });
        });

        it('falls back to a null statusFilter for an invalid status value', () => {
            const params = convertToParamMap({status: 'purple'});
            expect(parseElasticIndicesFiltersFromParams(params).statusFilter).toBeNull();
        });

    });


    describe('buildElasticIndicesQueryParams()', () => {

        it('includes all params when they differ from their defaults', () => {
            expect(buildElasticIndicesQueryParams({nameFilter: 'audit', statusFilter: 'red', hideSystemIndices: false}))
                .toEqual({indexName: 'audit', status: 'red', hideSystemIndices: 'false'});
        });

        it('omits indexName, status, and hideSystemIndices when all filters are at their defaults', () => {
            expect(buildElasticIndicesQueryParams({nameFilter: '', statusFilter: null, hideSystemIndices: true}))
                .toEqual({indexName: null, status: null, hideSystemIndices: null});
        });

    });


});
