export interface SearchResultPage<T> {
    results: T[];
    totalResultCount: number;
    offset: number;
    limit: number;
    firstResultIndex: number;
    lastResultIndex: number;
}
