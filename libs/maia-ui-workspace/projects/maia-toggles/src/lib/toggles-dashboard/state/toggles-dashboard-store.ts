import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {FeatureToggleResponseDto} from '../models/FeatureToggleResponseDto';
import {ActivationStrategyDefinitionDto} from '../models/ActivationStrategyDefinitionDto';
import {TogglesApiService} from '../services/toggles-api.service';
import {countDisabled, countEnabled, countOverdue, filterToggles, ToggleStateFilter} from './toggles-dashboard-filtering';

type TogglesDashboardState = {
    toggles: FeatureToggleResponseDto[];
    strategies: ActivationStrategyDefinitionDto[];
    isLoading: boolean;
    error: string | null;
    actionError: string | null;
    nameFilter: string;
    stateFilter: ToggleStateFilter;
    overdueOnly: boolean;
};

const initialState: TogglesDashboardState = {
    toggles: [],
    strategies: [],
    isLoading: false,
    error: null,
    actionError: null,
    nameFilter: '',
    stateFilter: 'all',
    overdueOnly: false,
};

export const TogglesDashboardStore = signalStore(

    withState(initialState),

    withComputed(({toggles, nameFilter, stateFilter, overdueOnly}) => {
        const visibleToggles = computed<FeatureToggleResponseDto[]>(() =>
            filterToggles(toggles(), nameFilter(), stateFilter(), overdueOnly())
        );
        const enabledCount = computed<number>(() => countEnabled(toggles()));
        const disabledCount = computed<number>(() => countDisabled(toggles()));
        const overdueCount = computed<number>(() => countOverdue(toggles()));
        return {visibleToggles, enabledCount, disabledCount, overdueCount};
    }),

    withMethods((store, togglesService = inject(TogglesApiService)) => ({

        fetchAllToggles: rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    togglesService.getAllToggles().pipe(
                        tapResponse({
                            next: (toggles) => patchState(store, {toggles, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load feature toggles.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        fetchStrategies: rxMethod<void>(
            pipe(
                switchMap(() =>
                    togglesService.getStrategies().pipe(
                        tapResponse({
                            next: (strategies) => patchState(store, {strategies}),
                            error: (err) => console.error(err),
                        })
                    )
                )
            )
        ),

        retryFetch(): void {
            this.fetchAllToggles();
        },

        onNameFilterChanged(value: string): void {
            patchState(store, {nameFilter: value});
        },

        onStateFilterChanged(value: ToggleStateFilter): void {
            patchState(store, {stateFilter: value});
        },

        onOverdueOnlyToggled(value: boolean): void {
            patchState(store, {overdueOnly: value});
        },

        onActionFailed(message: string): void {
            patchState(store, {actionError: message});
        },

        dismissActionError(): void {
            patchState(store, {actionError: null});
        },

    }))

);
