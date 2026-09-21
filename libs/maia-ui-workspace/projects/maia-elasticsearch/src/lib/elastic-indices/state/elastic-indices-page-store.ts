import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {debounceTime, switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
import {ElasticIndicesApiService} from '../services/elastic-indices-api-service';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {
    countByDisplayStatus,
    DisplayStatus,
    ElasticIndicesFilters,
    filterAndSortByName,
    filterBySystemIndices,
    filterByStatus
} from './elastic-indices-filtering';

type ElasticIndicesPageState = {
    hideSystemIndices: boolean;
    indexStateDtos: EsIndexStateDto[];
    isLoading: boolean;
    nameFilter: string;
    statusFilter: DisplayStatus | null;
    error: string | null;
};

const initialState: ElasticIndicesPageState = {
    hideSystemIndices: true,
    indexStateDtos: [],
    isLoading: false,
    nameFilter: '',
    statusFilter: null,
    error: null,
};

export const ElasticIndicesPageStore = signalStore(

    withState(initialState),

    withComputed(({indexStateDtos, hideSystemIndices, nameFilter, statusFilter}) => {
        const toggleFilteredIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterBySystemIndices(indexStateDtos(), hideSystemIndices())
        );
        const nameFilteredIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterAndSortByName(toggleFilteredIndexStateDtos(), nameFilter())
        );
        const statusCounts = computed<Record<DisplayStatus, number>>(() =>
            countByDisplayStatus(nameFilteredIndexStateDtos())
        );
        const visibleIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterByStatus(nameFilteredIndexStateDtos(), statusFilter())
        );
        return {toggleFilteredIndexStateDtos, nameFilteredIndexStateDtos, statusCounts, visibleIndexStateDtos};
    }),

    withMethods((store, pageService = inject(ElasticIndicesApiService)) => ({

        applyInitialFilters(filters: ElasticIndicesFilters): void {
            patchState(store, filters);
        },

        fetchAllIndices: rxMethod<void>(
            pipe(
                debounceTime(300),
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    pageService.getIndexDefinitions().pipe(
                        tapResponse({
                            next: (indexStateDtos) => patchState(store, {indexStateDtos, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load indices.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        retryFetch(): void {
            this.fetchAllIndices();
        },

        onHideSystemIndicesChanged(toggleChange: MatSlideToggleChange): void {
            patchState(store, {hideSystemIndices: toggleChange.checked});
        },

        onNameFilterChanged(value: string): void {
            patchState(store, {nameFilter: value});
        },

        onStatusFilterToggled(status: DisplayStatus): void {
            patchState(store, {statusFilter: store.statusFilter() === status ? null : status});
        },

    }))

);
