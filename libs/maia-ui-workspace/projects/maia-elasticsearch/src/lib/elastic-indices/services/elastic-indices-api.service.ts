import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
import {ELASTIC_INDICES_API_BASE_URL} from './elastic-indices-api-base-url.token';

@Injectable()
export class ElasticIndicesApiService {

    private baseUrl = inject(ELASTIC_INDICES_API_BASE_URL);

    constructor(private http: HttpClient) {}

    getIndexDefinitions(): Observable<EsIndexStateDto[]> {
        return this.http.get<EsIndexStateDto[]>(`${this.baseUrl}/elastic_indices_state`).pipe(
            catchError(this.handleError<EsIndexStateDto[]>('getIndicesState', []))
        );
    }

    createIndex(indexName: string) {
        this.http.post(`${this.baseUrl}/elastic_index/create/${indexName}`, null).pipe(
            catchError(this.handleError('createIndex'))
        ).subscribe();
    }

    onSetIndexVersionActive(indexName: string) {
        this.http.post(`${this.baseUrl}/elastic_index/set_active/${indexName}`, null).pipe(
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
