import {Routes} from '@angular/router';
import {allFieldTypesGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/all-field-types/all-field-types-gen-routes';
import {bravoGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/bravo-gen-routes';
import {compositePrimaryKeyGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-gen-routes';
import {leftManyGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-gen-routes';
import {simpleGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-gen-routes';
import {someVersionedGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/versioned/some-versioned-gen-routes';
import {userGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-gen-routes';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/home/home-page').then(
                (m) => m.HomePage,
            ),
    },
    ...allFieldTypesGenRoutes,
    ...bravoGenRoutes,
    ...compositePrimaryKeyGenRoutes,
    {
        path: 'left-many/view/:id',
        loadComponent: () =>
            import('./pages/left-many-view/left-many-view-page').then(
                (m) => m.LeftManyViewPage,
            ),
    },
    ...leftManyGenRoutes,
    ...simpleGenRoutes,
    ...someVersionedGenRoutes,
    ...userGenRoutes,
    {
        path: 'login',
        loadComponent: () =>
            import('./pages/login/login-page').then(
                (m) => m.LoginPage,
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
