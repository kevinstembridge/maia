import {Routes} from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('../pages/simple/simple-page.component').then(
                (m) => m.SimplePageComponent,
            ),
    },
];
