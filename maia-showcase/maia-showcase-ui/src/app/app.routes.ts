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
        path: 'all_field_types',
        loadComponent: () =>
            import('@app/pages/all-field-types-blotter/all-field-types-blotter-page').then(
                (m) => m.AllFieldTypesBlotterPage,
            ),
    },
    {
        path: 'bravo',
        loadComponent: () =>
            import('@app/pages/bravo-blotter/bravo-blotter-page').then(
                (m) => m.BravoBlotterPage,
            ),
    },
    {
        path: 'simple',
        loadComponent: () =>
            import('@app/pages/simple/simple-page.component').then(
                (m) => m.SimplePageComponent,
            ),
    },
    {
        path: 'login',
        loadComponent: () =>
            import('@app/pages/login/login-page.component').then(
                (m) => m.LoginPageComponent,
            ),
    },
    {
        path: 'some_versioned',
        loadComponent: () =>
            import('@app/pages/some-versioned/some-versioned-blotter-page').then(
                (m) => m.SomeVersionedBlotterPage,
            ),
    },
    {
        path: 'composite_pk',
        loadComponent: () =>
            import('@app/pages/composite-pk/composite-pk-blotter-page').then(
                (m) => m.CompositePkBlotterPage,
            ),
    },
    {
        path: 'users',
        loadComponent: () =>
            import('@app/pages/users-blotter/users-blotter-page').then(
                (m) => m.UsersBlotterPage,
            ),
    },
    {
        path: 'left_searchable',
        loadComponent: () =>
            import('@app/pages/left-searchable-blotter/left-searchable-blotter-page').then(
                (m) => m.LeftSearchableBlotterPage,
            ),
    },
    {
        path: 'elastic_indices',
        loadComponent: () =>
            import('@app/pages/elastic-indices/elastic-indices-page.component').then(
                (m) => m.ElasticIndicesPageComponent,
            ),
    },
    {
        path: 'jobs_dashboard',
        loadComponent: () =>
            import('@app/pages/jobs-dashboard/jobs-dashboard-page.component').then(
                (m) => m.JobsDashboardPageComponent,
            ),
    },
];
