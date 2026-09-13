import {EsIndexStateDto} from '../models/EsIndexStateDto';

const STATUS_DISPLAY_ORDER = ['green', 'yellow', 'red'];


export function filterBySystemIndices(indices: EsIndexStateDto[], hideSystemIndices: boolean): EsIndexStateDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: EsIndexStateDto[], nameFilter: string): EsIndexStateDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function countByStatus(indices: EsIndexStateDto[]): Record<string, number> {
    const counts: Record<string, number> = {};
    for (const index of indices) {
        const status = index.health?.status?.toLowerCase();
        if (!status) {
            continue;
        }
        counts[status] = (counts[status] ?? 0) + 1;
    }
    return counts;
}


export function buildIndexCountSummary(
    visibleCount: number,
    totalCount: number,
    statusCounts: Record<string, number>
): string {
    const indexWord = totalCount === 1 ? 'index' : 'indices';
    const countLabel = visibleCount === totalCount
        ? `${totalCount} ${indexWord}`
        : `${visibleCount} of ${totalCount} ${indexWord}`;

    const orderedStatuses = [
        ...STATUS_DISPLAY_ORDER,
        ...Object.keys(statusCounts).filter((status) => !STATUS_DISPLAY_ORDER.includes(status)).sort()
    ];

    const statusSegments = orderedStatuses
        .filter((status) => (statusCounts[status] ?? 0) > 0)
        .map((status) => `${statusCounts[status]} ${status}`);

    return statusSegments.length === 0
        ? countLabel
        : `${countLabel} · ${statusSegments.join(' · ')}`;
}
