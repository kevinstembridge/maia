import {InjectionToken} from '@angular/core';


export const PROPS_API_BASE_URL = new InjectionToken<string>(
    'propsApiBaseUrl',
    { factory: () => '/api/ops' }
);
