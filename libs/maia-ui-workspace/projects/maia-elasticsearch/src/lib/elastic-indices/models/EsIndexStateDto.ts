import {EsIndexSummaryDto} from './EsIndexSummaryDto';
import {EsIndexHealthDto} from './EsIndexHealthDto';

export class EsIndexStateDto {
    indexName!: string;
    indexExists!: boolean;
    summary!: EsIndexSummaryDto;
    health!: EsIndexHealthDto;
}
