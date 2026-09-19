import {
    ApplicationConfig, inject,
    provideAppInitializer,
    provideBrowserGlobalErrorListeners,
    provideZonelessChangeDetection
} from '@angular/core';
import {provideLuxonDateAdapter} from '@angular/material-luxon-adapter';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {appInitializer} from '@app/_init/app.initializer';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth-service';
import {HttpClient, provideHttpClient, withXsrfConfiguration} from '@angular/common/http';


export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideHttpClient(withXsrfConfiguration({})),
        provideLuxonDateAdapter(),
        provideZonelessChangeDetection(),
        provideRouter(routes),
        {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'outline'}},
        provideAppInitializer(() => {
            const initializerFn = appInitializer(inject(AuthService), inject(HttpClient));
            return initializerFn();
        }),
    ],
};
