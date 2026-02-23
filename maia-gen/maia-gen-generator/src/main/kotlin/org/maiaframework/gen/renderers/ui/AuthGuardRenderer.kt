package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class AuthGuardRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    init {

        addImport(from = "@angular/core", name = "Injectable")
        addImport(from = "@angular/core", name = "inject")
        addImport(from = "@angular/router", name = "ActivatedRouteSnapshot")
        addImport(from = "@angular/router", name = "CanActivate")
        addImport(from = "@angular/router", name = "CanActivateChild")
        addImport(from = "@angular/router", name = "CanMatch")
        addImport(from = "@angular/router", name = "Route")
        addImport(from = "@angular/router", name = "Router")
        addImport(from = "@angular/router", name = "RouterStateSnapshot")
        addImport(from = "@angular/router", name = "UrlSegment")
        addImport(from = "@angular/router", name = "UrlTree")
        addImport(from = "rxjs", name = "Observable")
        addImport(authoritiesDef.authServiceTypescriptImport)
        addImport(authoritiesDef.currentUserStoreTypescriptImport)
        addImport(authoritiesDef.enumDef.typescriptImport)

    }


    override fun renderedFilePath(): String {

        return authoritiesDef.authGuardRenderedFilePath

    }


    override fun renderSourceBody() {

        val authorityUqcn = authoritiesDef.enumDef.uqcn

        append(
            $$"""
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
            |        return this.checkForSignedInAndAuthorisedUser(route.data?.["authorities"] || []);
            |
            |    }
            |
            |
            |    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
            |
            |        return this.checkForSignedInAndAuthorisedUser(route.data?.["authorities"] || []);
            |
            |    }
            |
            |
            |    private checkForSignedInAndAuthorisedUser(authoritiesRequiredByRoute: $${authorityUqcn}[]) {
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
            |}""".trimMargin()
        )

    }


}
