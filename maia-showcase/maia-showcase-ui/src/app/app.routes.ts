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
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/all-field-types/all-field-types-blotter-page').then(
                (m) => m.AllFieldTypesBlotterPage,
            ),
    },
    {
        path: 'bravo-blotter',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/bravo-blotter-page').then(
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
        path: 'simple/create',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-entity-create-page').then(
                (m) => m.SimpleEntityCreatePage,
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
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-entity-edit-page').then(
                (m) => m.SimpleEntityEditPage,
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
        path: 'composite-primary-key/edit/:id',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-entity-edit-page').then(
                (m) => m.CompositePrimaryKeyEntityEditPage,
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
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/versioned/some-versioned-blotter-page').then(
                (m) => m.SomeVersionedBlotterPage,
            ),
    },
    {
        path: 'composite-pk-blotter',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-blotter-page').then(
                (m) => m.CompositePrimaryKeyBlotterPage,
            ),
    },
    {
        path: 'user-blotter',
        loadComponent: () =>
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-blotter-page').then(
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
            import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-blotter-page').then(
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
