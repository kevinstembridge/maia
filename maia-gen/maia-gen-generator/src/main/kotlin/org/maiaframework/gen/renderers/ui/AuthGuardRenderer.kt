package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class AuthGuardRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return authoritiesDef.authGuardRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |import {inject, Injectable} from '@angular/core';
            |import {
            |    ActivatedRouteSnapshot,
            |    CanActivate,
            |    CanActivateChild,
            |    CanMatch,
            |    Route,
            |    Router,
            |    RouterStateSnapshot,
            |    UrlSegment,
            |    UrlTree
            |} from '@angular/router';
            |import {Observable} from 'rxjs';
            |import {AuthService} from '@maia/maia-ui';
            |import {Authority} from '@app/gen-components/todo/Authority';
            |import {CurrentUserStore} from '@app/state/current-user.store';
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class AuthGuard implements CanActivate, CanActivateChild, CanMatch {
            |
            |
            |    readonly currentUserStore = inject(CurrentUserStore);
            |
            |
            |    constructor(
            |        private router: Router,
            |        private authService: AuthService
            |    ) {
            |    }
            |
            |
            |    canMatch(route: Route, segments: UrlSegment[]): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
            |
            |        return this.checkForSignedInAndAuthorisedUser(route.data.authorities);
            |
            |    }
            |
            |
            |    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
            |
            |        return this.checkForSignedInAndAuthorisedUser(route.data.authorities);
            |
            |    }
            |
            |
            |    private checkForSignedInAndAuthorisedUser(authoritiesRequiredByRoute: Authority[]) {
            |
            |        if (this.currentUserStore.isSignedIn() === false) {
            |
            |            let urlTree = this.router.createUrlTree(['/signin']);
            |
            |            const navigation = this.router.currentNavigation();
            |
            |            if (navigation) {
            |
            |                urlTree = this.router.createUrlTree(
            |                    ['/signin'],
            |                    {
            |                        queryParams: {return_url: navigation.extractedUrl.toString()},
            |                    }
            |                );
            |
            |            }
            |
            |            this.router.navigateByUrl(urlTree);
            |            return false;
            |
            |        }
            |
            |        if (this.authService.currentUserHasAnyOfThese(authoritiesRequiredByRoute)) {
            |            return true;
            |        }
            |
            |        this.router.navigate(['/']);
            |        return false;
            |
            |    }
            |
            |    canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
            |
            |        return this.canActivate(route, state);
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
