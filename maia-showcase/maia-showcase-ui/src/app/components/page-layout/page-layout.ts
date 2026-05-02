import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-page-layout',
  standalone: true,
  templateUrl: './page-layout.html'
})
export class PageLayout {

  @Input() pageTitle!: string;
  @Input() dataPageId?: string;

}
