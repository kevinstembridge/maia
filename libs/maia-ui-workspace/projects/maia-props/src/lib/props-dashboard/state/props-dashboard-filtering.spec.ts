import {convertToParamMap} from '@angular/router';
import {buildPropsQueryParams, countOverridden, filterProperties, parsePropsFiltersFromParams} from './props-dashboard-filtering';
import {PropertyResponseDto} from '../models/PropertyResponseDto';

function aProperty(overrides: Partial<PropertyResponseDto> = {}): PropertyResponseDto {
    return {
        propertyName: 'server.port',
        effectiveValue: '3200',
        isOverridden: false,
        isRedundant: false,
        environmentValue: '3200',
        sourceName: 'applicationConfig',
        lastModifiedByUsername: null,
        lastModifiedTimestamp: null,
        comment: null,
        reviewDate: null,
        ...overrides,
    };
}

describe('filterProperties', () => {

    it('returns all properties when nameFilter is empty and overriddenOnly is false', () => {
        const properties = [aProperty({propertyName: 'a'}), aProperty({propertyName: 'b'})];
        expect(filterProperties(properties, '', false, false, false).length).toBe(2);
    });

    it('filters case-insensitively by name substring', () => {
        const properties = [aProperty({propertyName: 'server.port'}), aProperty({propertyName: 'maia.props.web.base-url'})];
        const result = filterProperties(properties, 'PROPS', false, false, false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('restricts to overridden properties when overriddenOnly is true', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true}),
            aProperty({propertyName: 'b', isOverridden: false}),
        ];
        const result = filterProperties(properties, '', true, false, false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('a');
    });

    it('sorts results by property name ascending', () => {
        const properties = [aProperty({propertyName: 'zebra'}), aProperty({propertyName: 'alpha'})];
        const result = filterProperties(properties, '', false, false, false);
        expect(result.map(p => p.propertyName)).toEqual(['alpha', 'zebra']);
    });

    it('combines name filter and overriddenOnly filter, keeping only the intersection', () => {
        const properties = [
            aProperty({propertyName: 'maia.props.web.base-url', isOverridden: true}),
            aProperty({propertyName: 'maia.props.web.timeout', isOverridden: false}),
            aProperty({propertyName: 'server.port', isOverridden: true}),
        ];
        const result = filterProperties(properties, 'props', true, false, false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('restricts to redundant properties when redundantOnly is true', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true, isRedundant: true}),
            aProperty({propertyName: 'b', isOverridden: true, isRedundant: false}),
            aProperty({propertyName: 'c', isOverridden: false, isRedundant: false}),
        ];
        const result = filterProperties(properties, '', false, true, false);
        expect(result.map(p => p.propertyName)).toEqual(['a']);
    });

    it('combines name filter and redundantOnly filter, keeping only the intersection', () => {
        const properties = [
            aProperty({propertyName: 'maia.props.web.base-url', isOverridden: true, isRedundant: true}),
            aProperty({propertyName: 'maia.props.web.timeout', isOverridden: true, isRedundant: false}),
            aProperty({propertyName: 'server.port', isOverridden: true, isRedundant: true}),
        ];
        const result = filterProperties(properties, 'props', false, true, false);
        expect(result.map(p => p.propertyName)).toEqual(['maia.props.web.base-url']);
    });

    it('returns an empty array when given an empty array', () => {
        expect(filterProperties([], '', false, false, false)).toEqual([]);
    });

    describe('overdueOnly filtering', () => {

        beforeEach(() => {
            vi.useFakeTimers();
            vi.setSystemTime(new Date('2026-09-26T12:00:00Z'));
        });

        afterEach(() => {
            vi.useRealTimers();
        });

        it('restricts to properties with a reviewDate in the past when overdueOnly is true', () => {
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'b', reviewDate: '2026-10-01'}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

        it('excludes properties with a null reviewDate when overdueOnly is true', () => {
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'b', reviewDate: null}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

        it('excludes a property whose reviewDate is today when overdueOnly is true', () => {
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'b', reviewDate: '2026-09-26'}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

        it('combines name filter and overdueOnly filter, keeping only the intersection', () => {
            const properties = [
                aProperty({propertyName: 'maia.props.web.base-url', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'maia.props.web.timeout', reviewDate: '2026-10-01'}),
                aProperty({propertyName: 'server.port', reviewDate: '2026-09-01'}),
            ];
            const result = filterProperties(properties, 'props', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['maia.props.web.base-url']);
        });

        it('uses the local calendar date rather than the UTC date when they differ near midnight UTC', () => {
            vi.setSystemTime(new Date('2026-09-26T23:30:00Z'));
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-26'}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

    });

});

describe('countOverridden', () => {

    it('counts only overridden properties in a mixed set', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true}),
            aProperty({propertyName: 'b', isOverridden: false}),
            aProperty({propertyName: 'c', isOverridden: true}),
        ];
        expect(countOverridden(properties)).toBe(2);
    });

    it('returns 0 when no properties are overridden', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: false}),
            aProperty({propertyName: 'b', isOverridden: false}),
        ];
        expect(countOverridden(properties)).toBe(0);
    });

});

describe('parsePropsFiltersFromParams', () => {

    it('parses all filters when all params are present', () => {
        const params = convertToParamMap({propertyName: 'server.port', overriddenOnly: 'true', redundantOnly: null, overdueOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: 'server.port', overriddenOnly: true, redundantOnly: false, overdueOnly: true});
    });

    it('parses redundantOnly when present', () => {
        const params = convertToParamMap({redundantOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: '', overriddenOnly: false, redundantOnly: true, overdueOnly: false});
    });

    it('parses overdueOnly when present', () => {
        const params = convertToParamMap({overdueOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: true});
    });

    it('defaults overriddenOnly, redundantOnly and overdueOnly to false and nameFilter to empty string when no params are present', () => {
        expect(parsePropsFiltersFromParams(convertToParamMap({}))).toEqual({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: false});
    });

});

describe('buildPropsQueryParams', () => {

    it('includes all params when they differ from their defaults', () => {
        expect(buildPropsQueryParams({nameFilter: 'server.port', overriddenOnly: true, redundantOnly: false, overdueOnly: true}))
            .toEqual({propertyName: 'server.port', overriddenOnly: 'true', redundantOnly: null, overdueOnly: 'true'});
    });

    it('includes redundantOnly when true', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false, redundantOnly: true, overdueOnly: false}))
            .toEqual({propertyName: null, overriddenOnly: null, redundantOnly: 'true', overdueOnly: null});
    });

    it('includes overdueOnly when true', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: true}))
            .toEqual({propertyName: null, overriddenOnly: null, redundantOnly: null, overdueOnly: 'true'});
    });

    it('omits all params when filters are at their defaults', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: false}))
            .toEqual({propertyName: null, overriddenOnly: null, redundantOnly: null, overdueOnly: null});
    });

});
