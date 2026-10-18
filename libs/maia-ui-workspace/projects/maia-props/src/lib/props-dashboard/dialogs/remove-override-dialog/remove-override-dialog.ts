import {Component, inject, signal} from '@angular/core';
import {form, FormField} from '@angular/forms/signals';
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
    imports: [FormField, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class RemoveOverrideDialog {

    readonly dialogRef = inject(MatDialogRef<RemoveOverrideDialog>);

    readonly data = inject<RemoveOverrideDialogData>(MAT_DIALOG_DATA);

    private readonly model = signal({comment: ''});

    readonly form = form(this.model);

    onConfirm(event: Event) {

        event.preventDefault();

        const result: RemoveOverrideDialogResult = {
            comment: this.model().comment || null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
