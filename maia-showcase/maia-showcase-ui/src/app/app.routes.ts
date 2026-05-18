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
        path: 'all-field-types-blotter',
        loadComponent: () =>
            import('@app/pages/all-field-types-blotter/all-field-types-blotter-page').then(
                (m) => m.AllFieldTypesBlotterPage,
            ),
    },
    {
        path: 'bravo-blotter',
        loadComponent: () =>
            import('@app/pages/bravo-blotter/bravo-blotter-page').then(
                (m) => m.BravoBlotterPage,
            ),
    },
    {
        path: 'simple-blotter',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page').then(
                (m) => m.SimpleBlotterPage,
            ),
    },
    {
        path: 'simple/view/:id',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-entity-detail-view-page').then(
                (m) => m.SimpleEntityDetailViewPage,
            ),
    },
    {
        path: 'simple/edit/:id',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-entity-edit-form-page').then(
                (m) => m.SimpleEntityEditFormPage,
            ),
    },
    {
        path: 'composite-primary-key/view/:id',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-entity-detail-view-page').then(
                (m) => m.CompositePrimaryKeyEntityDetailViewPage,
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
        path: 'some-versioned-blotter',
        loadComponent: () =>
            import('./pages/some-versioned-blotter/some-versioned-blotter-page').then(
                (m) => m.SomeVersionedBlotterPage,
            ),
    },
    {
        path: 'composite-pk-blotter',
        loadComponent: () =>
            import('./pages/composite-pk-blotter/composite-pk-blotter-page').then(
                (m) => m.CompositePkBlotterPage,
            ),
    },
    {
        path: 'user-blotter',
        loadComponent: () =>
            import('./pages/user-blotter/user-blotter-page').then(
                (m) => m.UserBlotterPage,
            ),
    },
    {
        path: 'user-group-membership-blotter',
        loadComponent: () =>
            import('@app/pages/user-group-membership-blotter/user-group-membership-blotter-page').then(
                (m) => m.UserGroupMembershipBlotterPage,
            ),
    },
    {
        path: 'left-many-blotter',
        loadComponent: () =>
            import('./pages/left-many-blotter/left-many-blotter-page').then(
                (m) => m.LeftManyBlotterPage,
            ),
    },
    {
        path: 'left-many/view/:id',
        loadComponent: () =>
            import('./pages/left-many-view/left-many-view-page').then(
                (m) => m.LeftManyViewPage,
            ),
    },
    {
        path: 'elastic-indices',
        loadComponent: () =>
            import('./pages/elastic-indices/elastic-indices-page').then(
                (m) => m.ElasticIndicesPage,
            ),
    },
    {
        path: 'jobs-dashboard',
        loadComponent: () =>
            import('./pages/jobs-dashboard/jobs-dashboard-page').then(
                (m) => m.JobsDashboardPage,
            ),
    },
];
