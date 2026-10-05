import {InjectionToken} from '@angular/core';


export const TOGGLES_API_BASE_URL = new InjectionToken<string>(
    'togglesApiBaseUrl',
    { factory: () => '/api/ops/toggles' }
);
