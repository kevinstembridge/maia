import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';
import {JobsApiService} from '../../jobs-dashboard/services/jobs-api.service';
import {HistoryFilters, HistoryStatusFilter} from './job-history-filtering';

type JobHistoryState = {
    items: JobExecutionHistoryItem[];
    totalCount: number;
    isLoading: boolean;
    error: string | null;
    jobNameFilter: string | null;
    statusFilter: HistoryStatusFilter;
    fromDate: string | null;
    toDate: string | null;
    pageIndex: number;
    pageSize: number;
    availableJobNames: string[];
};

const initialState: JobHistoryState = {
    items: [],
    totalCount: 0,
    isLoading: false,
    error: null,
    jobNameFilter: null,
    statusFilter: null,
    fromDate: null,
    toDate: null,
    pageIndex: 0,
    pageSize: 20,
    availableJobNames: [],
};

export const JobHistoryStore = signalStore(

    withState(initialState),

    withMethods((store, jobsService = inject(JobsApiService)) => {

        const search = rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    jobsService.searchJobExecutionHistory({
                        jobName: store.jobNameFilter(),
                        status: store.statusFilter(),
                        from: store.fromDate(),
                        to: store.toDate(),
                        offset: store.pageIndex() * store.pageSize(),
                        limit: store.pageSize(),
                    }).pipe(
                        tapResponse({
                            next: (page) => patchState(store, {
                                items: page.results,
                                totalCount: page.totalResultCount,
                                isLoading: false,
                                error: null,
                            }),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load job history.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        );

        const loadAvailableJobNames = rxMethod<void>(
            pipe(
                switchMap(() =>
                    jobsService.getJobsState().pipe(
                        tapResponse({
                            next: (jobStates) => patchState(store, {
                                availableJobNames: [...new Set(jobStates.map((j) => j.jobName))].sort()
                            }),
                            error: (err) => console.error(err),
                        })
                    )
                )
            )
        );

        return {

            // Seeds filter state from the URL without triggering a search — the
            // subsequent init() call performs the one initial search, unlike the
            // onXChanged methods below which each patch state and search immediately.
            applyInitialFilters(filters: HistoryFilters): void {
                patchState(store, {...filters, pageIndex: 0});
            },

            init(): void {
                loadAvailableJobNames();
                search();
            },

            retryFetch(): void {
                search();
            },

            onJobNameFilterChanged(value: string | null): void {
                patchState(store, {jobNameFilter: value, pageIndex: 0});
                search();
            },

            onStatusFilterChanged(value: HistoryStatusFilter): void {
                patchState(store, {statusFilter: value, pageIndex: 0});
                search();
            },

            onDateRangeChanged(fromDate: string | null, toDate: string | null): void {
                patchState(store, {fromDate, toDate, pageIndex: 0});
                search();
            },

            onPageChanged(pageIndex: number, pageSize: number): void {
                patchState(store, {pageIndex, pageSize});
                search();
            },

        };

    })

);
