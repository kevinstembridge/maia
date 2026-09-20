import {Component, Inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

export interface EditPropertyDialogData {
    propertyName: string | null;
    currentValue: string | null;
}

export interface EditPropertyDialogResult {
    propertyName: string;
    propertyValue: string;
    comment: string | null;
}

@Component({
    selector: 'maia-edit-property-dialog',
    templateUrl: './edit-property-dialog.html',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class EditPropertyDialog {

    readonly isAdding = this.data.propertyName === null;

    readonly form = this.formBuilder.group({
        propertyName: this.formBuilder.control(this.data.propertyName ?? '', Validators.required),
        propertyValue: this.formBuilder.control(this.data.currentValue ?? '', Validators.required),
        comment: this.formBuilder.control(''),
    });

    constructor(
        public dialogRef: MatDialogRef<EditPropertyDialog>,
        @Inject(MAT_DIALOG_DATA) public data: EditPropertyDialogData,
        private formBuilder: FormBuilder
    ) {}

    onSubmit() {

        if (this.form.invalid) {
            return;
        }

        const value = this.form.getRawValue();

        const result: EditPropertyDialogResult = {
            propertyName: value.propertyName!,
            propertyValue: value.propertyValue!,
            comment: value.comment || null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
