
export interface SearchResultPage<T> {
  firstResultIndex: number;
  lastResultIndex: number;
  limit: number;
  offset: number;
  results: T[];
  totalResultCount: number;
}
