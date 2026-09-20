import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {PropertyResponseDto} from '../models/PropertyResponseDto';
import {PropsApiService} from '../services/props-api.service';
import {countOverridden, filterProperties} from './props-dashboard-filtering';

type PropsDashboardState = {
    properties: PropertyResponseDto[];
    isLoading: boolean;
    error: string | null;
    nameFilter: string;
    overriddenOnly: boolean;
};

const initialState: PropsDashboardState = {
    properties: [],
    isLoading: false,
    error: null,
    nameFilter: '',
    overriddenOnly: false,
};

export const PropsDashboardStore = signalStore(

    withState(initialState),

    withComputed(({properties, nameFilter, overriddenOnly}) => {
        const visibleProperties = computed<PropertyResponseDto[]>(() =>
            filterProperties(properties(), nameFilter(), overriddenOnly())
        );
        const overriddenCount = computed<number>(() =>
            countOverridden(properties())
        );
        return {visibleProperties, overriddenCount};
    }),

    withMethods((store, propsService = inject(PropsApiService)) => ({

        fetchAllProperties: rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    propsService.getAllProperties().pipe(
                        tapResponse({
                            next: (properties) => patchState(store, {properties, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load properties.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        retryFetch(): void {
            this.fetchAllProperties();
        },

        onNameFilterChanged(value: string): void {
            patchState(store, {nameFilter: value});
        },

        onOverriddenOnlyToggled(value: boolean): void {
            patchState(store, {overriddenOnly: value});
        },

        applyPropertyUpdate(updated: PropertyResponseDto): void {
            const properties = store.properties().filter(p => p.propertyName !== updated.propertyName);
            patchState(store, {properties: [...properties, updated]});
        },

        applyPropertyRemoval(propertyName: string): void {
            patchState(store, {properties: store.properties().filter(p => p.propertyName !== propertyName)});
        },

    }))

);
