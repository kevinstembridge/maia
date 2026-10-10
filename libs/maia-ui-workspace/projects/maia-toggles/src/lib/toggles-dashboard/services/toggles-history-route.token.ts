import {InjectionToken} from '@angular/core';


/**
 * The app route of the generated feature toggle history blotter page, without the trailing
 * toggle id. The host app must register that route, with the exported `FeatureToggleHistoryBlotterPage`
 * as its component and its own authority guard (path `<route>/:id`).
 */
export const TOGGLES_HISTORY_ROUTE = new InjectionToken<string>(
    'togglesHistoryRoute',
    { factory: () => '/ops/toggles/feature-toggle/history' }
);
