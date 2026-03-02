import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth.service';
import {LoginRequestDto} from '@app/gen-components/org/maiaframework/showcase/auth/LoginRequestDto';
import {CurrentUserStore} from '@app/gen-components/org/maiaframework/showcase/auth/current-user.store';


@Component({
    selector: 'app-login-form-wrapper',
    templateUrl: './login-page.component.html'
})
export class LoginFormWrapperComponent implements OnInit {


    accountLocked = signal(false);


    badCredentials = signal(false);


    private returnUrl!: string;


    private navigationState: any = null;


    private readonly currentUserStore = inject(CurrentUserStore);


    private readonly authService = inject(AuthService);


    private readonly router = inject(Router);


    private readonly route = inject(ActivatedRoute);


    ngOnInit(): void {

        if (this.currentUserStore.isLoggedIn()) {
            this.router.navigate(['/']);
        }

        this.navigationState = history.state;
        this.returnUrl = this.route.snapshot.queryParams['return_url'] || '/';

    }


    handleFormSubmission(loginRequestDto: LoginRequestDto) {

        this.authService.authenticate(loginRequestDto).subscribe({
            next: () => {
                this.router.navigateByUrl(this.returnUrl, {state: this.navigationState});
            },
            error: err => {
                this.accountLocked.set(err.errorCode === 'ACCOUNT_LOCKED');
                this.badCredentials.set(err.errorCode === 'BAD_CREDENTIALS');
            }
        });

    }


}
