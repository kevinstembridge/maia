import {Component, computed, inject, signal} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {CurrentUserStore} from '@app/gen-components/org/maiaframework/showcase/auth/current-user.store';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth.service';
import {Authority} from '@app/gen-components/org/maiaframework/showcase/auth/Authority';
import {CurrentUserAuthStore} from '@app/state/current-user-auth.store';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, MatMenu, MatMenuItem, MatMenuTrigger],
    templateUrl: './app.html',
    styleUrl: './app.css',
})
export class App {


    protected readonly title = signal('Maia Showcase');


    protected readonly menuOpen = signal(false);


    private readonly currentUserStore = inject(CurrentUserStore);


    private readonly currentUserAuthStore = inject(CurrentUserAuthStore);


    private readonly authService = inject(AuthService);


    protected readonly isLoggedIn = this.currentUserStore.isLoggedIn;


    protected readonly hasReadAuthority = computed(() =>
        this.currentUserAuthStore.hasReadAuthority()
    );


    protected readonly hasSysOpsAuthority = computed(() =>
        this.currentUserAuthStore.hasSysOpsAuthority()
    );


    protected toggleMenu(): void {

        this.menuOpen.update((open) => !open);

    }


    protected logout(): void {

        this.authService.logout();

    }


}
