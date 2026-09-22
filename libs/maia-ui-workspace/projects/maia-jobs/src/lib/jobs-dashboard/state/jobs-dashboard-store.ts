import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap, timer} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {JobState} from '../models/JobState';
import {JobsApiService} from '../services/jobs-api.service';
import {countByStatus, deriveJobStatus, filterAndSortByName, JobStatus} from './jobs-filtering';

const POLL_INTERVAL_MS = 15_000;

type JobsDashboardState = {
    jobStates: JobState[];
    isLoading: boolean;
    error: string | null;
    nameFilter: string;
    statusFilter: JobStatus | null;
    now: number;
};

const initialState: JobsDashboardState = {
    jobStates: [],
    isLoading: false,
    error: null,
    nameFilter: '',
    statusFilter: null,
    now: Date.now(),
};

export const JobsDashboardStore = signalStore(

    withState(initialState),

    withComputed(({jobStates, nameFilter, statusFilter}) => {
        const nameFilteredJobStates = computed<JobState[]>(() =>
            filterAndSortByName(jobStates(), nameFilter())
        );
        const statusCounts = computed<Record<string, number>>(() =>
            countByStatus(nameFilteredJobStates())
        );
        const totalCount = computed<number>(() => nameFilteredJobStates().length);
        const visibleJobStates = computed<JobState[]>(() => {
            const status = statusFilter();
            return status === null
                ? nameFilteredJobStates()
                : nameFilteredJobStates().filter((it) => deriveJobStatus(it) === status);
        });
        return {visibleJobStates, statusCounts, totalCount};
    }),

    withMethods((store, jobsService = inject(JobsApiService)) => {

        const fetchOnce = rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true, now: Date.now()})),
                switchMap(() =>
                    jobsService.getJobsState().pipe(
                        tapResponse({
                            next: (jobStates) => patchState(store, {jobStates, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load jobs.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        );

        return {

            startPolling: rxMethod<void>(
                pipe(
                    switchMap(() => timer(0, POLL_INTERVAL_MS).pipe(tap(() => fetchOnce())))
                )
            ),

            retryFetch(): void {
                fetchOnce();
            },

            onNameFilterChanged(value: string): void {
                patchState(store, {nameFilter: value});
            },

            onStatusFilterChanged(value: JobStatus | null): void {
                patchState(store, {statusFilter: value});
            },

        };

    })

);
