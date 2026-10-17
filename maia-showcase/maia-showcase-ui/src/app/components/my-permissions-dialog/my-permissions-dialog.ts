import {Component, inject, ChangeDetectionStrategy} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {CurrentUserStore} from '@app/gen-components/org/maiaframework/showcase/current-user-store';


@Component({
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule],
    selector: 'app-my-permissions-dialog',
    changeDetection: ChangeDetectionStrategy.Eager,
    templateUrl: './my-permissions-dialog.html',
})
export class MyPermissionsDialog {


    private readonly dialogRef = inject(MatDialogRef<MyPermissionsDialog>);


    protected readonly currentUserStore = inject(CurrentUserStore);


    protected close(): void {
        this.dialogRef.close();
    }


}
