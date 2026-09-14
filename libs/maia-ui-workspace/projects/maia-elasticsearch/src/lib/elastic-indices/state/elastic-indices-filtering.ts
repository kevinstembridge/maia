import {EsIndexStateDto} from '../models/EsIndexStateDto';

export type DisplayStatus = 'green' | 'yellow' | 'red' | 'not-created';

export const STATUS_TILE_ORDER: DisplayStatus[] = ['green', 'yellow', 'red', 'not-created'];

export const STATUS_COLORS: Record<DisplayStatus, string> = {
    green: '#4caf50',
    yellow: '#fbc02d',
    red: '#d32f2f',
    'not-created': '#9e9e9e',
};


export function filterBySystemIndices(indices: EsIndexStateDto[], hideSystemIndices: boolean): EsIndexStateDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: EsIndexStateDto[], nameFilter: string): EsIndexStateDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function deriveDisplayStatus(index: EsIndexStateDto): DisplayStatus | undefined {
    if (!index.indexExists) {
        return 'not-created';
    }
    const status = index.health?.status?.toLowerCase();
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
