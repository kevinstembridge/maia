import {convertToParamMap} from '@angular/router';
import {Settings} from 'luxon';
import {buildTogglesQueryParams, countDisabled, countEnabled, countOverdue, filterToggles, isOverdue, parseTogglesFiltersFromParams} from './toggles-dashboard-filtering';
import {FeatureToggleResponseDto} from '../models/FeatureToggleResponseDto';

function aToggle(overrides: Partial<FeatureToggleResponseDto> = {}): FeatureToggleResponseDto {
    return {
        id: '00000000-0000-0000-0000-000000000001',
        featureName: 'SampleFeature',
        enabled: false,
        description: null,
        ticketKey: null,
        reviewDate: null,
        contactPerson: null,
        infoLink: null,
        attributes: null,
        comment: null,
        activationStrategies: [],
        lastModifiedBy: 'SYSTEM',
        lastModifiedTimestamp: '2026-09-26T10:00:00Z',
        createdTimestamp: '2026-09-26T10:00:00Z',
        version: 1,
        ...overrides,
    };
}

describe('toggles filtering', () => {

    beforeEach(() => {
        Settings.now = () => new Date('2026-09-27T12:00:00Z').valueOf();
    });

    afterEach(() => {
        Settings.now = () => Date.now();
    });

    describe('isOverdue', () => {

        it('is false when there is no review date', () => {
            expect(isOverdue(null)).toBe(false);
        });

        it('is true when the review date is before today', () => {
            expect(isOverdue('2026-09-26')).toBe(true);
        });

        it('is false when the review date is today or later', () => {
            expect(isOverdue('2026-09-27')).toBe(false);
            expect(isOverdue('2026-10-01')).toBe(false);
        });

    });

    describe('filterToggles', () => {

        const toggles = [
            aToggle({featureName: 'Bravo', enabled: true}),
            aToggle({featureName: 'alpha', enabled: false, reviewDate: '2026-09-01'}),
            aToggle({featureName: 'Charlie', enabled: true, reviewDate: '2026-09-01'}),
        ];

        it('returns all toggles sorted by name when no filters are set', () => {
            expect(filterToggles(toggles, '', 'all', false).map(t => t.featureName)).toEqual(['alpha', 'Bravo', 'Charlie']);
        });

        it('does not mutate the input', () => {
            filterToggles(toggles, '', 'all', false);
            expect(toggles.map(t => t.featureName)).toEqual(['Bravo', 'alpha', 'Charlie']);
        });

        it('filters case-insensitively by name substring, ignoring surrounding whitespace', () => {
            expect(filterToggles(toggles, '  ALPH ', 'all', false).map(t => t.featureName)).toEqual(['alpha']);
        });

        it('restricts to enabled toggles', () => {
            expect(filterToggles(toggles, '', 'enabled', false).map(t => t.featureName)).toEqual(['Bravo', 'Charlie']);
        });

        it('restricts to disabled toggles', () => {
            expect(filterToggles(toggles, '', 'disabled', false).map(t => t.featureName)).toEqual(['alpha']);
        });

        it('restricts to overdue toggles', () => {
            expect(filterToggles(toggles, '', 'all', true).map(t => t.featureName)).toEqual(['alpha', 'Charlie']);
        });

        it('combines all filters', () => {
            expect(filterToggles(toggles, 'char', 'enabled', true).map(t => t.featureName)).toEqual(['Charlie']);
            expect(filterToggles(toggles, 'char', 'disabled', true)).toEqual([]);
        });

    });

    describe('counts', () => {

        const toggles = [
            aToggle({enabled: true}),
            aToggle({enabled: true, reviewDate: '2026-09-01'}),
            aToggle({enabled: false, reviewDate: '2026-12-01'}),
        ];

        it('counts enabled, disabled and overdue toggles', () => {
            expect(countEnabled(toggles)).toBe(2);
            expect(countDisabled(toggles)).toBe(1);
            expect(countOverdue(toggles)).toBe(1);
        });

        it('returns 0 for an empty list', () => {
            expect(countEnabled([])).toBe(0);
            expect(countDisabled([])).toBe(0);
            expect(countOverdue([])).toBe(0);
        });

    });

    describe('parseTogglesFiltersFromParams', () => {

        it('parses all filters when present', () => {
            expect(parseTogglesFiltersFromParams(convertToParamMap({featureName: 'Foo', state: 'enabled', overdueOnly: 'true'})))
                .toEqual({nameFilter: 'Foo', stateFilter: 'enabled', overdueOnly: true});
        });

        it('parses the disabled state', () => {
            expect(parseTogglesFiltersFromParams(convertToParamMap({state: 'disabled'})).stateFilter).toBe('disabled');
        });

        it('defaults when no params are present', () => {
            expect(parseTogglesFiltersFromParams(convertToParamMap({})))
                .toEqual({nameFilter: '', stateFilter: 'all', overdueOnly: false});
        });

        it('ignores an unrecognised state', () => {
            expect(parseTogglesFiltersFromParams(convertToParamMap({state: 'bogus'})).stateFilter).toBe('all');
        });

    });

    describe('buildTogglesQueryParams', () => {

        it('includes params that differ from their defaults', () => {
            expect(buildTogglesQueryParams({nameFilter: 'Foo', stateFilter: 'disabled', overdueOnly: true}))
                .toEqual({featureName: 'Foo', state: 'disabled', overdueOnly: 'true'});
        });

        it('omits params at their defaults', () => {
            expect(buildTogglesQueryParams({nameFilter: '', stateFilter: 'all', overdueOnly: false}))
                .toEqual({featureName: null, state: null, overdueOnly: null});
        });

    });

});
