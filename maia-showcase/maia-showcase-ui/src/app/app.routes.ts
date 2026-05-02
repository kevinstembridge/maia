import {Routes} from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/home/home-page').then(
                (m) => m.HomePage,
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
            import('./pages/simple/simple-page').then(
                (m) => m.SimplePage,
            ),
    },
    {
        path: 'login',
        loadComponent: () =>
            import('./pages/login/login-page').then(
                (m) => m.LoginPage,
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
        path: 'user_group_memberships',
        loadComponent: () =>
            import('@app/pages/user-group-membership-blotter/user-group-membership-blotter-page').then(
                (m) => m.UserGroupMembershipBlotterPage,
            ),
    },
    {
        path: 'left_many_searchable',
        loadComponent: () =>
            import('./pages/left-many-searchable-blotter/left-many-searchable-blotter-page').then(
                (m) => m.LeftManySearchableBlotterPage,
            ),
    },
    {
        path: 'left_many/view/:id',
        loadComponent: () =>
            import('./pages/left-many-view/left-many-view-page').then(
                (m) => m.LeftManyViewPage,
            ),
    },
    {
        path: 'elastic_indices',
        loadComponent: () =>
            import('./pages/elastic-indices/elastic-indices-page').then(
                (m) => m.ElasticIndicesPage,
            ),
    },
    {
        path: 'jobs_dashboard',
        loadComponent: () =>
            import('./pages/jobs-dashboard/jobs-dashboard-page').then(
                (m) => m.JobsDashboardPage,
            ),
    },
];
