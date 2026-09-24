import {ManagedEsIndexInfoDto} from './ManagedEsIndexInfoDto';
import {EsIndexHealthDto} from './EsIndexHealthDto';


export class EsIndexStateDto {
    exists!: boolean;
    health?: EsIndexHealthDto;
    indexName!: string;
    managedIndexInfo?: ManagedEsIndexInfoDto;
}
