import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';

@Component({
    selector: 'maia-create-index-dialog',
    templateUrl: './create-index-dialog.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class CreateIndexDialog {

    constructor(
        public dialogRef: MatDialogRef<CreateIndexDialog>,
        @Inject(MAT_DIALOG_DATA) public dto: EsIndexStateDto
    ) {}

    onSubmit() {
        this.dialogRef.close(true);
    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
