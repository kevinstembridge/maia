import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {PropertyHistoryItemResponseDto} from '../../models/PropertyHistoryItemResponseDto';

export interface PropertyHistoryDialogData {
    propertyName: string;
    historyItems: PropertyHistoryItemResponseDto[];
}

@Component({
    selector: 'maia-property-history-dialog',
    templateUrl: './property-history-dialog.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class PropertyHistoryDialog {

    constructor(
        public dialogRef: MatDialogRef<PropertyHistoryDialog>,
        @Inject(MAT_DIALOG_DATA) public data: PropertyHistoryDialogData
    ) {}

    onClose(): void {
        this.dialogRef.close();
    }

}
