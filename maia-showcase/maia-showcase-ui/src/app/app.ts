import {Component, signal} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, MatMenu, MatMenuItem, MatMenuTrigger],
    templateUrl: './app.html',
    styleUrl: './app.css',
})
export class App {


    protected readonly title = signal('Maia Showcase');


    protected readonly menuOpen = signal(false);


    protected toggleMenu(): void {

        this.menuOpen.update((open) => !open);

    }


}
