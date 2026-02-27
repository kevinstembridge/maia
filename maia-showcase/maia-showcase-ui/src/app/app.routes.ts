import {Routes} from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('../pages/simple-page-component/simple-page.component').then(
                (m) => m.SimplePageComponent,
            ),
    },
];
