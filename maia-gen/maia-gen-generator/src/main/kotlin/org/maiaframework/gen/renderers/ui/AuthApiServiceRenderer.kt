package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class AuthApiServiceRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    init {

        addImport(from = "@angular/common/http", name = "HttpClient")
        addImport(from = "@angular/core", name = "inject")
        addImport(from = "@angular/core", name = "Injectable")
        addImport(from = "rxjs", name = "Observable")
        addImport(authoritiesDef.loginRequestDtoTypescriptImport)
        addImport(authoritiesDef.userSummaryDtoTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return authoritiesDef.authApiServiceRenderedFilePath

    }


    override fun renderSourceBody() {

        append(
            """
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class AuthApiService {
            |
            |
            |    private readonly http = inject(HttpClient);
            |
            |
            |    authenticate(loginRequestDto: LoginRequestDto): Observable<UserSummaryDto> {
            |
            |        return this.http.post<any>('/api/login', loginRequestDto, {withCredentials: true});
            |
            |    }
            |
            |
            |    refreshCurrentUser(): Observable<UserSummaryDto> {
            |
            |        return this.http.get<any>('/api/current_user', {withCredentials: true});
            |
            |    }
            |
            |
            |    logout() {
            |
            |        console.log('logging out current user');
            |        this.http.post<any>('/logout', {}, {withCredentials: true}).subscribe();
            |
            |    }
            |
            |
            |}""".trimMargin()
        )

    }


}
