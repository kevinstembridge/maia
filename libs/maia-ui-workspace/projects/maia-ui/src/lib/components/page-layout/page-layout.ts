import {Component, Input, ChangeDetectionStrategy} from '@angular/core';

@Component({
  selector: 'maia-page-layout',
  standalone: true,
  styleUrl: './page-layout.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './page-layout.html'
})
export class PageLayout {

  @Input() pageTitle!: string;
  @Input() dataPageId?: string;

}
