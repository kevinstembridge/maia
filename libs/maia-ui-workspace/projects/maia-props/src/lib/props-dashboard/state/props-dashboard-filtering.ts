import {ParamMap, Params} from '@angular/router';
import {PropertyResponseDto} from '../models/PropertyResponseDto';


export interface PropsFilters {
    nameFilter: string;
    overriddenOnly: boolean;
}


export function filterProperties(
    properties: PropertyResponseDto[],
    nameFilter: string,
    overriddenOnly: boolean
): PropertyResponseDto[] {

    const normalizedFilter = nameFilter.trim().toLowerCase();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));

}


export function countOverridden(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isOverridden).length;

}


export function parsePropsFiltersFromParams(params: ParamMap): PropsFilters {

    const rawOverriddenOnly = params.get('overriddenOnly');

    return {
        nameFilter: params.get('propertyName') ?? '',
        overriddenOnly: rawOverriddenOnly === null ? false : rawOverriddenOnly === 'true',
    };

}


export function buildPropsQueryParams(filters: PropsFilters): Params {
    return {
        propertyName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        overriddenOnly: filters.overriddenOnly === false ? null : 'true',
    };
}
