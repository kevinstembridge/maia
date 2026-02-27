import {Component, signal} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule],
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
