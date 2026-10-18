import {Component, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import {form, FormField} from '@angular/forms/signals';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

export interface ToggleStateDialogData {
    featureName: string;
    enable: boolean;
}

export interface ToggleStateDialogResult {
    comment: string | null;
}

@Component({
    selector: 'maia-toggle-state-dialog',
    templateUrl: './toggle-state-dialog.html',
    styleUrl: './toggle-state-dialog.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormField, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class ToggleStateDialog {

    readonly data = inject<ToggleStateDialogData>(MAT_DIALOG_DATA);

    private readonly dialogRef = inject(MatDialogRef<ToggleStateDialog>);

    private readonly model = signal({comment: ''});

    readonly form = form(this.model);

    onConfirm(event: Event): void {

        event.preventDefault();

        const result: ToggleStateDialogResult = {
            comment: this.model().comment.trim() || null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
