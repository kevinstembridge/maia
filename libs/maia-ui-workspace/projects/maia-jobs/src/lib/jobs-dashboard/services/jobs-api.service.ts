import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {JobState} from '../models/JobState';
import {JobExecutionDetail} from '../models/JobExecutionDetail';
import {JobExecutionHistoryItem} from '../models/JobExecutionHistoryItem';
import {SearchResultPage} from '../models/SearchResultPage';
import {JOBS_API_BASE_URL} from './jobs-api-base-url.token';


@Injectable()
export class JobsApiService {


    private baseUrl = inject(JOBS_API_BASE_URL);


    constructor(private http: HttpClient) {}


    getJobsState(): Observable<JobState[]> {

        return this.http.get<JobState[]>(`${this.baseUrl}/jobs/current_state`).pipe(
            catchError(this.handleError<JobState[]>('getJobsState', []))
        );

    }


    getJobExecutionDetail(jobExecutionId: string): Observable<JobExecutionDetail> {

        return this.http.get<JobExecutionDetail>(`${this.baseUrl}/job/execution_detail/${jobExecutionId}`).pipe(
            catchError(this.handleError<JobExecutionDetail>('getJobExecutionDetail'))
        );

    }


    searchJobExecutionHistory(criteria: {
        jobName: string | null;
        status: string | null;
        from: string | null;
        to: string | null;
        offset: number;
        limit: number;
    }): Observable<SearchResultPage<JobExecutionHistoryItem>> {

        let params = new HttpParams()
            .set('offset', criteria.offset)
            .set('limit', criteria.limit);

        if (criteria.jobName) {
            params = params.set('jobName', criteria.jobName);
        }
        if (criteria.status) {
            params = params.set('status', criteria.status);
        }
        if (criteria.from) {
            params = params.set('from', criteria.from);
        }
        if (criteria.to) {
            params = params.set('to', criteria.to);
        }

        return this.http.get<SearchResultPage<JobExecutionHistoryItem>>(`${this.baseUrl}/job/execution_history`, {params}).pipe(
            catchError(this.handleError<SearchResultPage<JobExecutionHistoryItem>>('searchJobExecutionHistory', {
                results: [],
                totalResultCount: 0,
                offset: criteria.offset,
                limit: criteria.limit,
                firstResultIndex: criteria.offset + 1,
                lastResultIndex: criteria.offset,
            }))
        );

    }


    getStacktrace(jobExecutionId: string): Observable<any> {

        return this.http.get<any>(`${this.baseUrl}/job/execution_stacktrace/${jobExecutionId}`);

    }


    runJob(jobName: string): void {

        this.http.post(`${this.baseUrl}/job/run/${jobName}`, null).pipe(
            catchError(this.handleError('runJob'))
        ).subscribe();

    }


    private handleError<T>(operation = 'operation', result?: T) {

        return (error: any): Observable<T> => {
            console.error(error);
            return of(result as T);
        };

    }


}
