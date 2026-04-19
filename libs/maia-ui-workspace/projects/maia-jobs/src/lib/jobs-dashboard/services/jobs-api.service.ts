import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {JobState} from '../models/JobState';
import {JobExecutionSummary} from '../models/JobExecutionSummary';
import {JobExecutionDetail} from '../models/JobExecutionDetail';
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


    getRecentlyFailedJobExecutions(jobName: string): Observable<JobExecutionSummary[]> {

        return this.http.get<JobExecutionSummary[]>(`${this.baseUrl}/job/recently_failed/${jobName}`).pipe(
            catchError(this.handleError<JobExecutionSummary[]>('getRecentlyFailedJobExecutions', []))
        );

    }


    getJobExecutionDetail(jobExecutionId: string): Observable<JobExecutionDetail> {

        return this.http.get<JobExecutionDetail>(`${this.baseUrl}/job/execution_detail/${jobExecutionId}`).pipe(
            catchError(this.handleError<JobExecutionDetail>('getJobExecutionDetail'))
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
