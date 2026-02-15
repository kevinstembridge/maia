import { Component, Input, OnInit } from '@angular/core';
import { MessageDetails } from './message-details';
import { KeyValuePipe } from '@angular/common';


@Component({
    imports: [KeyValuePipe],
    selector: 'app-message-panel',
    templateUrl: './message-panel.component.html'
})
export class MessagePanelComponent implements OnInit {

  @Input() messageDetails: MessageDetails;

  constructor() { }

  ngOnInit(): void {
  }

}
