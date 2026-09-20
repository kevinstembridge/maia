import {Component, Inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

export interface RemoveOverrideDialogData {
    propertyName: string;
}

export interface RemoveOverrideDialogResult {
    comment: string | null;
}

@Component({
    selector: 'maia-remove-override-dialog',
    templateUrl: './remove-override-dialog.html',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class RemoveOverrideDialog {

    readonly form = this.formBuilder.group({
        comment: this.formBuilder.control(''),
    });

    constructor(
        public dialogRef: MatDialogRef<RemoveOverrideDialog>,
        @Inject(MAT_DIALOG_DATA) public data: RemoveOverrideDialogData,
        private formBuilder: FormBuilder
    ) {}

    onConfirm() {

        const result: RemoveOverrideDialogResult = {
            comment: this.form.getRawValue().comment || null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
