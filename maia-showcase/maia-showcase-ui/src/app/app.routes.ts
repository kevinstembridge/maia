import {Routes} from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('@app/pages/home/home-page.component').then(
                (m) => m.HomePageComponent,
            ),
    },
    {
        path: 'simple',
        loadComponent: () =>
            import('@app/pages/simple/simple-page.component').then(
                (m) => m.SimplePageComponent,
            ),
    },
];
