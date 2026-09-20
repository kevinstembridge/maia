import {countOverridden, filterProperties} from './props-dashboard-filtering';
import {PropertyResponseDto} from '../models/PropertyResponseDto';

function aProperty(overrides: Partial<PropertyResponseDto> = {}): PropertyResponseDto {
    return {
        propertyName: 'server.port',
        effectiveValue: '3200',
        isOverridden: false,
        environmentValue: '3200',
        sourceName: 'applicationConfig',
        lastModifiedByUsername: null,
        lastModifiedTimestamp: null,
        comment: null,
        ...overrides,
    };
}

describe('filterProperties', () => {

    it('returns all properties when nameFilter is empty and overriddenOnly is false', () => {
        const properties = [aProperty({propertyName: 'a'}), aProperty({propertyName: 'b'})];
        expect(filterProperties(properties, '', false).length).toBe(2);
    });

    it('filters case-insensitively by name substring', () => {
        const properties = [aProperty({propertyName: 'server.port'}), aProperty({propertyName: 'maia.props.web.base-url'})];
        const result = filterProperties(properties, 'PROPS', false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('restricts to overridden properties when overriddenOnly is true', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true}),
            aProperty({propertyName: 'b', isOverridden: false}),
        ];
        const result = filterProperties(properties, '', true);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('a');
    });

    it('sorts results by property name ascending', () => {
        const properties = [aProperty({propertyName: 'zebra'}), aProperty({propertyName: 'alpha'})];
        const result = filterProperties(properties, '', false);
        expect(result.map(p => p.propertyName)).toEqual(['alpha', 'zebra']);
    });

    it('combines name filter and overriddenOnly filter, keeping only the intersection', () => {
        const properties = [
            aProperty({propertyName: 'maia.props.web.base-url', isOverridden: true}),
            aProperty({propertyName: 'maia.props.web.timeout', isOverridden: false}),
            aProperty({propertyName: 'server.port', isOverridden: true}),
        ];
        const result = filterProperties(properties, 'props', true);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('returns an empty array when given an empty array', () => {
        expect(filterProperties([], '', false)).toEqual([]);
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
