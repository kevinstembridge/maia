import {InjectionToken} from '@angular/core';


export const JOBS_API_BASE_URL = new InjectionToken<string>(
    'jobsApiBaseUrl',
    { factory: () => '/api/ops' }
);
