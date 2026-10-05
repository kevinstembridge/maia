import {ParamMap, Params} from '@angular/router';
import {DateTime} from 'luxon';
import {FeatureToggleResponseDto} from '../models/FeatureToggleResponseDto';


export type ToggleStateFilter = 'all' | 'enabled' | 'disabled';


export interface TogglesFilters {
    nameFilter: string;
    stateFilter: ToggleStateFilter;
    overdueOnly: boolean;
}


export const TOGGLES_TILE_COLORS = {
    total: '#9e9e9e',
    enabled: '#388e3c',
    disabled: '#757575',
    overdue: '#d32f2f',
} as const;


function todayIsoString(): string {
    return DateTime.local().toISODate()!;
}


export function isOverdue(reviewDate: string | null): boolean {

    return reviewDate !== null && reviewDate < todayIsoString();

}


export function filterToggles(
    toggles: FeatureToggleResponseDto[],
    nameFilter: string,
    stateFilter: ToggleStateFilter,
    overdueOnly: boolean
): FeatureToggleResponseDto[] {

    const normalizedFilter = nameFilter.trim().toLowerCase();

    return toggles
        .filter(t => stateFilter === 'all' || t.enabled === (stateFilter === 'enabled'))
        .filter(t => !overdueOnly || isOverdue(t.reviewDate))
        .filter(t => normalizedFilter === '' || t.featureName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.featureName.localeCompare(b.featureName));

}


export function countEnabled(toggles: FeatureToggleResponseDto[]): number {

    return toggles.filter(t => t.enabled).length;

}


export function countDisabled(toggles: FeatureToggleResponseDto[]): number {

    return toggles.filter(t => !t.enabled).length;

}


export function countOverdue(toggles: FeatureToggleResponseDto[]): number {

    return toggles.filter(t => isOverdue(t.reviewDate)).length;

}


export function parseTogglesFiltersFromParams(params: ParamMap): TogglesFilters {

    const rawState = params.get('state');
    const rawOverdueOnly = params.get('overdueOnly');

    return {
        nameFilter: params.get('featureName') ?? '',
        stateFilter: rawState === 'enabled' || rawState === 'disabled' ? rawState : 'all',
        overdueOnly: rawOverdueOnly === 'true',
    };

}


export function buildTogglesQueryParams(filters: TogglesFilters): Params {

    return {
        featureName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        state: filters.stateFilter === 'all' ? null : filters.stateFilter,
        overdueOnly: filters.overdueOnly ? 'true' : null,
    };

}
