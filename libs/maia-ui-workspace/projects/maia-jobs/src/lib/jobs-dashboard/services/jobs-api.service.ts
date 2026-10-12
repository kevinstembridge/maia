import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {JobStateResponseDto} from '@app/gen-components/org/maiaframework/job/JobStateResponseDto';
import {JobExecutionDetailResponseDto} from '@app/gen-components/org/maiaframework/job/JobExecutionDetailResponseDto';
import {JobExecutionHistoryItemResponseDto} from '@app/gen-components/org/maiaframework/job/JobExecutionHistoryItemResponseDto';
import {SearchResultPage} from '@maia/maia-ui';
import {JOBS_API_BASE_URL} from './jobs-api-base-url.token';


@Injectable()
export class JobsApiService {


    private baseUrl = inject(JOBS_API_BASE_URL);


    constructor(private http: HttpClient) {}


    getJobsState(): Observable<JobStateResponseDto[]> {

        return this.http.get<JobStateResponseDto[]>(`${this.baseUrl}/jobs/current_state`).pipe(
            catchError(this.handleError<JobStateResponseDto[]>('getJobsState', []))
        );

    }


    getJobExecutionDetail(jobExecutionId: string): Observable<JobExecutionDetailResponseDto> {

        return this.http.get<JobExecutionDetailResponseDto>(`${this.baseUrl}/job/execution_detail/${jobExecutionId}`).pipe(
            catchError(this.handleError<JobExecutionDetailResponseDto>('getJobExecutionDetail'))
        );

    }


    searchJobExecutionHistory(criteria: {
        jobName: string | null;
        status: string | null;
        from: string | null;
        to: string | null;
        offset: number;
        limit: number;
    }): Observable<SearchResultPage<JobExecutionHistoryItemResponseDto>> {

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

        return this.http.get<SearchResultPage<JobExecutionHistoryItemResponseDto>>(`${this.baseUrl}/job/execution_history`, {params});

    }


    getStacktrace(jobExecutionId: string): Observable<any> {

        return this.http.get<any>(`${this.baseUrl}/job/execution_stacktrace/${jobExecutionId}`);

    }


    runJob(jobName: string): void {

        this.http.post(`${this.baseUrl}/job/run/${jobName}`, null).pipe(
            catchError(this.handleError('runJob'))
        ).subscribe();

    }


    abandonJobExecution(jobExecutionId: string): Observable<void> {

        return this.http.post<void>(`${this.baseUrl}/job/execution_abandon/${jobExecutionId}`, null);

    }


    private handleError<T>(operation = 'operation', result?: T) {

        return (error: any): Observable<T> => {
            console.error(error);
            return of(result as T);
        };

    }


}
