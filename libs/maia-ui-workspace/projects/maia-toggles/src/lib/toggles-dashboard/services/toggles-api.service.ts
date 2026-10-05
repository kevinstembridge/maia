import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {FeatureToggleResponseDto} from '../models/FeatureToggleResponseDto';
import {ActivationStrategyDefinitionDto} from '../models/ActivationStrategyDefinitionDto';
import {ActivationStrategyDescriptor} from '../models/ActivationStrategyDescriptor';
import {TOGGLES_API_BASE_URL} from './toggles-api-base-url.token';


@Injectable()
export class TogglesApiService {


    private readonly http = inject(HttpClient);

    private readonly baseUrl = inject(TOGGLES_API_BASE_URL);


    getAllToggles(): Observable<FeatureToggleResponseDto[]> {

        return this.http.get<FeatureToggleResponseDto[]>(`${this.baseUrl}/toggles`);

    }


    getStrategies(): Observable<ActivationStrategyDefinitionDto[]> {

        return this.http.get<ActivationStrategyDefinitionDto[]>(`${this.baseUrl}/strategies`);

    }


    setToggle(featureName: string, enabled: boolean, comment: string | null, version: number): Observable<void> {

        return this.http.post<void>(`${this.baseUrl}/set-feature-toggle`, {
            featureName,
            enabled,
            comment,
            version,
        });

    }


    updateActivationStrategies(id: string, version: number, activationStrategies: ActivationStrategyDescriptor[]): Observable<void> {

        return this.http.put<void>(`${this.baseUrl}/feature-toggle/inline/activation-strategies`, {
            id,
            version,
            activationStrategies,
        });

    }


}
