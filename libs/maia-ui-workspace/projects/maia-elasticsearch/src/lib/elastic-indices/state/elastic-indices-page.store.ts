import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {debounceTime, distinctUntilChanged, switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
import {ElasticIndicesApiService} from '../services/elastic-indices-api.service';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';

type ElasticIndicesPageState = {
    hideSystemIndices: boolean;
    indexStateDtos: EsIndexStateDto[];
    isLoading: boolean;
};

const initialState: ElasticIndicesPageState = {
    hideSystemIndices: true,
    indexStateDtos: [],
    isLoading: false,
};

export const ElasticIndicesPageStore = signalStore(

    withState(initialState),

    withComputed(({indexStateDtos, hideSystemIndices}) => ({
        visibleIndexStateDtos: computed<EsIndexStateDto[]>(() =>
            indexStateDtos().filter((it) =>
                hideSystemIndices() === false || it.indexName.startsWith('.') === false
            )
        )
    })),

    withMethods((store, pageService = inject(ElasticIndicesApiService)) => ({

        fetchAllIndices: rxMethod<void>(
            pipe(
                debounceTime(300),
                distinctUntilChanged(),
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    pageService.getIndexDefinitions().pipe(
                        tapResponse({
                            next: (indexStateDtos) => patchState(store, {indexStateDtos, isLoading: false}),
                            error: (err) => {
                                patchState(store, {isLoading: false});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        onHideSystemIndicesChanged(toggleChange: MatSlideToggleChange): void {
            patchState(store, {hideSystemIndices: toggleChange.checked});
        },

    }))

);
