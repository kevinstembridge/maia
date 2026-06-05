import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-page-layout',
  standalone: true,
  styleUrl: './page-layout.scss',
  templateUrl: './page-layout.html'
})
export class PageLayout {

  @Input() pageTitle!: string;
  @Input() dataPageId?: string;

}
