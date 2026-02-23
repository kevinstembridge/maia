package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class AuthServiceTypescriptRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    init {

        addImport(from = "@angular/core", name = "Injectable")
        addImport(from = "@angular/core", name = "inject")
        addImport(from = "rxjs", name = "Observable")
        addImport(from = "rxjs", name = "throwError")
        addImport(from = "rxjs/operators", name = "catchError")
        addImport(from = "rxjs/operators", name = "map")
        addImport(from = "@angular/router", name = "Router")
        addImport(from = "@angular/common/http", name = "HttpErrorResponse")
        addImport(authoritiesDef.enumDef.typescriptImport)
        addImport(authoritiesDef.userSummaryDtoTypescriptImport)
        addImport(authoritiesDef.authApiServiceTypescriptImport)
        addImport(from = "@maia/maia-ui", name = "ProblemDetail")

    }


    override fun renderedFilePath(): String {

        return authoritiesDef.authServiceRenderedFilePath

    }


    override fun renderSourceBody() {

        val authorityUqcn = authoritiesDef.enumDef.uqcn

        append(
            $$"""
            |import {CurrentUserStore} from '@app/state/current-user.store';
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class AuthService {
            |
            |
            |    private readonly currentUserStore = inject(CurrentUserStore);
            |
            |
            |    constructor(
            |        private apiService: AuthApiService,
            |        private router: Router
            |    ) { }
            |
            |
            |    public currentUserHasThisAuthority(authority: $$authorityUqcn): boolean {
            |
            |        return !!this.currentUserStore.currentUser()
            |            && this.currentUserStore.currentUser().grantedAuthorities.includes(authority);
            |
            |    }
            |
            |
            |    public currentUserHasAnyOfThese(authorities: $${authorityUqcn}[]): boolean {
            |
            |        return !!this.currentUserStore.currentUser()
            |            && this.currentUserStore.currentUser().grantedAuthorities.find(e => authorities.includes(e)) != null;
            |
            |    }
            |
            |
            |    authenticate(signinRequestDto: SigninRequestDto): Observable<UserSummaryDto> {
            |
            |        return this.apiService.authenticate(signinRequestDto)
            |            .pipe(
            |                map(user => {
            |                    this.currentUserStore.setCurrentUser(user);
            |                    return user;
            |                }),
            |                catchError(this.handleSigninError)
            |            );
            |
            |    }
            |
            |
            |    refreshToken(): Observable<UserSummaryDto> {
            |
            |        return this.apiService.refreshCurrentUser()
            |            .pipe(
            |                map(user => {
            |                    this.currentUserStore.setCurrentUser(user);
            |                    return user;
            |                })
            |            );
            |
            |    }
            |
            |
            |    logout() {
            |
            |        this.apiService.logout();
            |        this.currentUserStore.setCurrentUser(null);
            |        this.router.navigate(['/']);
            |
            |    }
            |
            |
            |    private handleSigninError(error: HttpErrorResponse) {
            |
            |        if (error.error instanceof ErrorEvent) {
            |            // A client-side or network error occurred. Handle it accordingly.
            |            console.error('An error occurred:', error.error.message);
            |        } else {
            |
            |            if (error.status === 423) {
            |                return throwError(() => ({errorCode: 'ACCOUNT_LOCKED', errorMessage: 'Account locked'}));
            |            }
            |
            |            if (error.status === 401) {
            |                return throwError(() => ({errorCode: 'BAD_CREDENTIALS', errorMessage: 'Bad credentials'}));
            |            }
            |
            |            // The backend returned an unsuccessful response code.
            |            // The response body may contain clues as to what went wrong,
            |            console.error(`Backend returned code ${error.status}, body was: ${error.error}`);
            |        }
            |
            |        return throwError(() => 'Something bad happened; please try again later.');
            |
            |    }
            |
            |
            |}""".trimMargin())

    }


}
