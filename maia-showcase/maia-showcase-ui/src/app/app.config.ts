import {
    ApplicationConfig, inject,
    provideAppInitializer,
    provideBrowserGlobalErrorListeners,
    provideZonelessChangeDetection
} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {appInitializer} from '@app/_init/app.initializer';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth.service';
import {provideHttpClient} from '@angular/common/http';


export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideHttpClient(),
        provideZonelessChangeDetection(),
        provideRouter(routes),
        provideAppInitializer(() => {
            const initializerFn = (appInitializer)(inject(AuthService));
            return initializerFn();
        }),
    ],
};
