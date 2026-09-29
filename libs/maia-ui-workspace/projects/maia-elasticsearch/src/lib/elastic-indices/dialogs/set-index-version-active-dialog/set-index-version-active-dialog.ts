import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
import {MatButtonModule} from '@angular/material/button';

@Component({
    selector: 'maia-set-index-version-active-dialog',
    templateUrl: './set-index-version-active-dialog.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class SetIndexVersionActiveDialog {

    constructor(
        public dialogRef: MatDialogRef<SetIndexVersionActiveDialog>,
        @Inject(MAT_DIALOG_DATA) public dto: IndexStateResponseDto
    ) {}

    onSubmit() {
        this.dialogRef.close(true);
    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
