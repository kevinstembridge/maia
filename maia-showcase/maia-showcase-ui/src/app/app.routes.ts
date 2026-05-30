import {Routes} from '@angular/router';
import {allFieldTypesRoutes} from '@app/gen-components/org/maiaframework/showcase/all-field-types/all-field-types-routes';
import {bravoRoutes} from '@app/gen-components/org/maiaframework/showcase/join/bravo-routes';
import {compositePrimaryKeyRoutes} from '@app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-routes';
import {leftManyRoutes} from '@app/gen-components/org/maiaframework/showcase/many-to-many/left-many-routes';
import {simpleRoutes} from '@app/gen-components/org/maiaframework/showcase/simple/simple-routes';
import {someVersionedRoutes} from '@app/gen-components/org/maiaframework/showcase/versioned/some-versioned-routes';
import {userRoutes} from '@app/gen-components/org/maiaframework/showcase/user/user-routes';
import {alphaRoutes} from '@app/gen-components/org/maiaframework/showcase/join/alpha-routes';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/home/home-page').then(
                (m) => m.HomePage,
            ),
    },
    ...allFieldTypesRoutes,
    ...alphaRoutes,
    ...bravoRoutes,
    ...compositePrimaryKeyRoutes,
    ...leftManyRoutes,
    ...simpleRoutes,
    ...someVersionedRoutes,
    ...userRoutes,
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
