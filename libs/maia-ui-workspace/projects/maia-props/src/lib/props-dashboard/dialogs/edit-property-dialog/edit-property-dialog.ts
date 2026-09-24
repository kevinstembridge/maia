import {Component, Inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {DateTime} from 'luxon';

export interface EditPropertyDialogData {
    propertyName: string | null;
    currentValue: string | null;
    currentReviewDate: string | null;
}

export interface EditPropertyDialogResult {
    propertyName: string;
    propertyValue: string;
    comment: string | null;
    reviewDate: string | null;
}

@Component({
    selector: 'maia-edit-property-dialog',
    templateUrl: './edit-property-dialog.html',
    styleUrl: './edit-property-dialog.scss',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule, MatDatepickerModule]
})
export class EditPropertyDialog {

    readonly isAdding: boolean;
    readonly form;

    constructor(
        public dialogRef: MatDialogRef<EditPropertyDialog>,
        @Inject(MAT_DIALOG_DATA) public data: EditPropertyDialogData,
        private formBuilder: FormBuilder
    ) {

        this.isAdding = this.data.propertyName === null;

        this.form = this.formBuilder.group({
            propertyName: this.formBuilder.control(this.data.propertyName ?? '', Validators.required),
            propertyValue: this.formBuilder.control(this.data.currentValue ?? '', Validators.required),
            comment: this.formBuilder.control(''),
            reviewDate: this.formBuilder.control<DateTime | null>(this.data.currentReviewDate ? DateTime.fromISO(this.data.currentReviewDate) : null),
        });

    }

    onSubmit() {

        if (this.form.invalid) {
            return;
        }

        const value = this.form.getRawValue();

        const result: EditPropertyDialogResult = {
            propertyName: value.propertyName!,
            propertyValue: value.propertyValue!,
            comment: value.comment || null,
            reviewDate: value.reviewDate?.toISODate() ?? null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
