import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
import {ELASTIC_INDICES_API_BASE_URL} from './elastic-indices-api-base-url-token';

@Injectable()
export class ElasticIndicesApiService {

    private baseUrl = inject(ELASTIC_INDICES_API_BASE_URL);

    constructor(private http: HttpClient) {}

    getIndexDefinitions(): Observable<IndexStateResponseDto[]> {
        return this.http.get<IndexStateResponseDto[]>(`${this.baseUrl}/elastic_indices_state`).pipe(
            catchError(this.handleError<IndexStateResponseDto[]>('getIndicesState', []))
        );
    }

    createIndex(indexBaseName: string, indexVersion: number): Observable<unknown> {
        return this.http.post(`${this.baseUrl}/elastic_index/create/${indexBaseName}/${indexVersion}`, null).pipe(
            catchError(this.handleError('createIndex'))
        );
    }

    onSetIndexVersionActive(indexBaseName: string, indexVersion: number) {
        this.http.post(`${this.baseUrl}/elastic_index/set_active/${indexBaseName}/${indexVersion}`, null).pipe(
            catchError(this.handleError('onSetIndexVersionActive'))
        ).subscribe();
    }

    private handleError<T>(operation = 'operation', result?: T) {
        return (error: any): Observable<T> => {
            console.error(error);
            return of(result as T);
        };
    }

}
