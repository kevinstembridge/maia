import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {EsIndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/EsIndexStateResponseDto';
import {MatButtonModule} from '@angular/material/button';

@Component({
    selector: 'maia-create-index-dialog',
    templateUrl: './create-index-dialog.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class CreateIndexDialog {

    constructor(
        public dialogRef: MatDialogRef<CreateIndexDialog>,
        @Inject(MAT_DIALOG_DATA) public dto: EsIndexStateResponseDto
    ) {}

    onSubmit() {
        this.dialogRef.close(true);
    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
