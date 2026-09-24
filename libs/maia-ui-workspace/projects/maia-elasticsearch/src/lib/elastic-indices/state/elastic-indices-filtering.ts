import {ParamMap, Params} from '@angular/router';
import {EsIndexStateDto} from '../models/EsIndexStateDto';

export type DisplayStatus = 'green' | 'yellow' | 'red' | 'not-created';

export const STATUS_TILE_ORDER: DisplayStatus[] = ['green', 'yellow', 'red', 'not-created'];

export const STATUS_COLORS: Record<DisplayStatus, string> = {
    green: '#4caf50',
    yellow: '#fbc02d',
    red: '#d32f2f',
    'not-created': '#9e9e9e',
};

export interface ElasticIndicesFilters {
    nameFilter: string;
    statusFilter: DisplayStatus | null;
    hideSystemIndices: boolean;
}

const VALID_STATUS_FILTERS: string[] = ['green', 'yellow', 'red', 'not-created'];


export function filterBySystemIndices(indices: EsIndexStateDto[], hideSystemIndices: boolean): EsIndexStateDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: EsIndexStateDto[], nameFilter: string): EsIndexStateDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function deriveDisplayStatus(indexStateDto: EsIndexStateDto): DisplayStatus | undefined {
    if (!indexStateDto.exists) {
        return 'not-created';
    }
    const status = indexStateDto.health?.status?.toLowerCase();
    return status === 'green' || status === 'yellow' || status === 'red' ? status : undefined;
}


export function countByDisplayStatus(indices: EsIndexStateDto[]): Record<DisplayStatus, number> {
    const counts: Record<DisplayStatus, number> = {green: 0, yellow: 0, red: 0, 'not-created': 0};
    for (const index of indices) {
        const status = deriveDisplayStatus(index);
        if (status) {
            counts[status]++;
        }
    }
    return counts;
}


export function filterByStatus(indices: EsIndexStateDto[], status: DisplayStatus | null): EsIndexStateDto[] {
    return status === null ? indices : indices.filter((it) => deriveDisplayStatus(it) === status);
}


export function parseElasticIndicesFiltersFromParams(params: ParamMap): ElasticIndicesFilters {

    const rawStatus = params.get('status');
    const statusFilter = rawStatus !== null && VALID_STATUS_FILTERS.includes(rawStatus)
        ? rawStatus as DisplayStatus
        : null;

    const rawHideSystemIndices = params.get('hideSystemIndices');
    const hideSystemIndices = rawHideSystemIndices === null ? true : rawHideSystemIndices === 'true';

    return {
        nameFilter: params.get('indexName') ?? '',
        statusFilter,
        hideSystemIndices,
    };

}


export function buildElasticIndicesQueryParams(filters: ElasticIndicesFilters): Params {
    return {
        indexName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        status: filters.statusFilter,
        hideSystemIndices: filters.hideSystemIndices === true ? null : 'false',
    };
}
