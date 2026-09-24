import {EsIndexName} from './EsIndexName';

export class ManagedEsIndexInfoDto {
    indexName!: EsIndexName;
    description!: string;
    isActiveVersion!: boolean;
}
