
import { TotalHits } from './TotalHits';

export interface IndexSearchResult<T> {
  hits: T[];
  firstResultIndex: number;
  lastResultIndex: number;
  totalHits: TotalHits;
}
