import {
    ApplicationConfig, inject,
    provideAppInitializer,
    provideBrowserGlobalErrorListeners,
    provideZonelessChangeDetection
} from '@angular/core';
import {provideNativeDateAdapter} from '@angular/material/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {appInitializer} from '@app/_init/app.initializer';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth.service';
import {HttpClient, provideHttpClient, withXsrfConfiguration} from '@angular/common/http';


export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideHttpClient(withXsrfConfiguration({})),
        provideNativeDateAdapter(),
        provideZonelessChangeDetection(),
        provideRouter(routes),
        provideAppInitializer(() => {
            const initializerFn = appInitializer(inject(AuthService), inject(HttpClient));
            return initializerFn();
        }),
    ],
};
