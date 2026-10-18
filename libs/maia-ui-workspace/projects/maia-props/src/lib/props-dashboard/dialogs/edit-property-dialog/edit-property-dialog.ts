import {Component, inject, signal} from '@angular/core';
import {form, FormField, readonly, required} from '@angular/forms/signals';
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
    isSensitive: boolean;
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
    imports: [FormField, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule, MatDatepickerModule]
})
export class EditPropertyDialog {

    readonly dialogRef = inject(MatDialogRef<EditPropertyDialog>);

    readonly data = inject<EditPropertyDialogData>(MAT_DIALOG_DATA);

    readonly isAdding = this.data.propertyName === null;

    private readonly model = signal({
        propertyName: this.data.propertyName ?? '',
        propertyValue: this.data.currentValue ?? '',
        comment: '',
        reviewDate: (this.data.currentReviewDate ? DateTime.fromISO(this.data.currentReviewDate) : null) as DateTime | null,
    });

    readonly form = form(this.model, (schemaPath) => {
        required(schemaPath.propertyName);
        required(schemaPath.propertyValue);
        readonly(schemaPath.propertyName, () => !this.isAdding);
    });

    onSubmit(event: Event) {

        event.preventDefault();

        if (this.form().invalid()) {
            return;
        }

        const value = this.model();

        const result: EditPropertyDialogResult = {
            propertyName: value.propertyName,
            propertyValue: value.propertyValue,
            comment: value.comment || null,
            reviewDate: value.reviewDate?.toISODate() ?? null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
