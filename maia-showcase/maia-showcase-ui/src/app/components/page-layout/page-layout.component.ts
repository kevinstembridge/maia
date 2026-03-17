import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-page-layout',
  standalone: true,
  templateUrl: './page-layout.component.html'
})
export class PageLayoutComponent {

  @Input() pageTitle!: string;
  @Input() dataPageId?: string;

}
