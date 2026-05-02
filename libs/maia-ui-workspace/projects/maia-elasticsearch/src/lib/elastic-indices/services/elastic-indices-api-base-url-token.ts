import {InjectionToken} from '@angular/core';

export const ELASTIC_INDICES_API_BASE_URL = new InjectionToken<string>(
    'elasticIndicesApiBaseUrl',
    { factory: () => '/api/ops' }
);
