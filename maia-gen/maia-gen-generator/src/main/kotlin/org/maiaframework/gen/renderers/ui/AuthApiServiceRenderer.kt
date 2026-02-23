package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class AuthApiServiceRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return authoritiesDef.authApiServiceRenderedFilePath

    }


    override fun renderSourceBody() {

        val userSummaryDtoImportPath = "@${authoritiesDef.userSummaryDtoRenderedFilePath.removeSuffix(".ts")}"

        append("""
            |import {HttpClient} from '@angular/common/http';
            |import {Observable} from 'rxjs';
            |import {Injectable} from '@angular/core';
            |import {UserSummaryDto} from '$userSummaryDtoImportPath';
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class AuthApiService {
            |
            |
            |    constructor(private http: HttpClient) {}
            |
            |
            |    authenticate(signinRequestDto: SigninRequestDto): Observable<UserSummaryDto> {
            |
            |        return this.http.post<any>('/api/signin', signinRequestDto, {withCredentials: true});
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
            |}""".trimMargin())

    }


}
