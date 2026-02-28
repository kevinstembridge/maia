import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {LoginFormComponent} from '@app/gen-components/org/maiaframework/showcase/login/login-form.component';
import {LoginFormApiService} from '@app/gen-components/org/maiaframework/showcase/login/login-form-api.service';
import {LoginRequestDto} from '@app/gen-components/org/maiaframework/showcase/login/LoginRequestDto';


@Component({
    selector: 'app-login-page',
    templateUrl: './login-page.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [LoginFormComponent]
})
export class LoginPageComponent {


    private readonly loginService = inject(LoginFormApiService);


    private readonly router = inject(Router);


    protected readonly error = signal<string | null>(null);


    onFormSubmission(dto: LoginRequestDto) {
        this.error.set(null);
        this.loginService.sendRequest(dto).subscribe({
            next: () => this.router.navigate(['/']),
            error: () => this.error.set('Login failed. Please check your credentials.')
        });
    }


}
