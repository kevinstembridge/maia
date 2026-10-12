import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {FeatureToggleResponseDto} from '@app/gen-components/org/maiaframework/toggles/FeatureToggleResponseDto';
import {ActivationStrategyDefinitionDto} from '../models/ActivationStrategyDefinitionDto';
import {ActivationStrategyDescriptor} from '@app/gen-components/org/maiaframework/toggles/activation/ActivationStrategyDescriptor';
import {SetFeatureToggleRequestDto} from '@app/gen-components/org/maiaframework/toggles/SetFeatureToggleRequestDto';
import {FeatureToggleUpdateRequestDto} from '@app/gen-components/org/maiaframework/toggles/FeatureToggleUpdateRequestDto';
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

        const request: SetFeatureToggleRequestDto = {
            featureName,
            enabled,
            comment: comment ?? undefined,
            version,
        };

        return this.http.post<void>(`${this.baseUrl}/set-feature-toggle`, request);

    }


    updateActivationStrategies(id: string, version: number, activationStrategies: ReadonlyArray<ActivationStrategyDescriptor>): Observable<void> {

        const request: FeatureToggleUpdateRequestDto = {
            id,
            version,
            activationStrategies,
        };

        return this.http.put<void>(`${this.baseUrl}/feature-toggle/inline/activation-strategies`, request);

    }


}
