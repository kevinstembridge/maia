import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {debounceTime, distinctUntilChanged, switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
import {ElasticIndicesApiService} from '../services/elastic-indices-api-service';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {buildIndexCountSummary, countByStatus, filterAndSortByName, filterBySystemIndices} from './elastic-indices-filtering';

type ElasticIndicesPageState = {
    hideSystemIndices: boolean;
    indexStateDtos: EsIndexStateDto[];
    isLoading: boolean;
    nameFilter: string;
    error: string | null;
};

const initialState: ElasticIndicesPageState = {
    hideSystemIndices: true,
    indexStateDtos: [],
    isLoading: false,
    nameFilter: '',
    error: null,
};

export const ElasticIndicesPageStore = signalStore(

    withState(initialState),

    withComputed(({indexStateDtos, hideSystemIndices, nameFilter}) => {
        const toggleFilteredIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterBySystemIndices(indexStateDtos(), hideSystemIndices())
        );
        const visibleIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterAndSortByName(toggleFilteredIndexStateDtos(), nameFilter())
        );
        const statusCounts = computed<Record<string, number>>(() =>
            countByStatus(visibleIndexStateDtos())
        );
        const countSummary = computed<string>(() =>
            buildIndexCountSummary(
                visibleIndexStateDtos().length,
                toggleFilteredIndexStateDtos().length,
                statusCounts()
            )
        );
        return {toggleFilteredIndexStateDtos, visibleIndexStateDtos, statusCounts, countSummary};
    }),

    withMethods((store, pageService = inject(ElasticIndicesApiService)) => ({

        fetchAllIndices: rxMethod<void>(
            pipe(
                debounceTime(300),
                distinctUntilChanged(),
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

    }))

);
