import {Component, computed, inject, signal} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {CurrentUserStore} from '@app/gen-components/org/maiaframework/showcase/current-user-store';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth-service';
import {CurrentUserAuthStore} from './state/current-user-auth-store';
import {MyPermissionsDialog} from './components/my-permissions-dialog/my-permissions-dialog';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, MatMenu, MatMenuItem, MatMenuTrigger],
    templateUrl: './app.html',
    styleUrl: './app.scss',
})
export class App {


    protected readonly title = signal('Maia Showcase');


    protected readonly menuOpen = signal(false);


    private readonly currentUserStore = inject(CurrentUserStore);


    private readonly currentUserAuthStore = inject(CurrentUserAuthStore);


    private readonly authService = inject(AuthService);


    private readonly dialog = inject(MatDialog);


    protected readonly isLoggedIn = this.currentUserStore.isLoggedIn;


    protected readonly hasReadAuthority = computed(() =>
        this.currentUserAuthStore.hasReadAuthority()
    );


    protected readonly hasWriteAuthority = computed(() =>
        this.currentUserAuthStore.hasWriteAuthority()
    );


    protected readonly hasMaiaJobReadAuthority = computed(() =>
        this.currentUserAuthStore.hasMaiaJobReadAuthority()
    );


    protected readonly hasMaiaJobWriteAuthority = computed(() =>
        this.currentUserAuthStore.hasMaiaJobWriteAuthority()
    );


    protected toggleMenu(): void {

        this.menuOpen.update((open) => !open);

    }


    protected logout(): void {

        this.authService.logout();

    }


    protected openMyPermissions(): void {

        this.dialog.open(MyPermissionsDialog);

    }


}
