import {ParamMap, Params} from '@angular/router';
import {DateTime} from 'luxon';
import {PropertyResponseDto} from '../models/PropertyResponseDto';


export interface PropsFilters {
    nameFilter: string;
    overriddenOnly: boolean;
    redundantOnly: boolean;
    overdueOnly: boolean;
}


function todayIsoString(): string {
    return DateTime.local().toISODate()!;
}


export function isOverdue(reviewDate: string | null): boolean {

    return reviewDate !== null && reviewDate < todayIsoString();

}


export function filterProperties(
    properties: PropertyResponseDto[],
    nameFilter: string,
    overriddenOnly: boolean,
    redundantOnly: boolean,
    overdueOnly: boolean
): PropertyResponseDto[] {

    const normalizedFilter = nameFilter.trim().toLowerCase();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => !redundantOnly || p.isRedundant)
        .filter(p => !overdueOnly || isOverdue(p.reviewDate))
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));

}


export function countOverridden(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isOverridden).length;

}


export function countRedundant(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isRedundant).length;

}


export function countOverdue(properties: PropertyResponseDto[]): number {

    return properties.filter(p => isOverdue(p.reviewDate)).length;

}


export function parsePropsFiltersFromParams(params: ParamMap): PropsFilters {

    const rawOverriddenOnly = params.get('overriddenOnly');
    const rawRedundantOnly = params.get('redundantOnly');
    const rawOverdueOnly = params.get('overdueOnly');

    return {
        nameFilter: params.get('propertyName') ?? '',
        overriddenOnly: rawOverriddenOnly === null ? false : rawOverriddenOnly === 'true',
        redundantOnly: rawRedundantOnly === null ? false : rawRedundantOnly === 'true',
        overdueOnly: rawOverdueOnly === null ? false : rawOverdueOnly === 'true',
    };

}


export function buildPropsQueryParams(filters: PropsFilters): Params {
    return {
        propertyName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        overriddenOnly: filters.overriddenOnly === false ? null : 'true',
        redundantOnly: filters.redundantOnly === false ? null : 'true',
        overdueOnly: filters.overdueOnly === false ? null : 'true',
    };
}
