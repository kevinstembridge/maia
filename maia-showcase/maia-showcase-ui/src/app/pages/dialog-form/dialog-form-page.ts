import {Component, inject, signal} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {FeedbackRequestDto} from '@app/gen-components/org/maiaframework/showcase/dialog/FeedbackRequestDto';
import {FeedbackDialog} from '@app/gen-components/org/maiaframework/showcase/dialog/feedback-dialog';


@Component({
    selector: 'app-dialog-form-page',
    imports: [MatButtonModule],
    templateUrl: './dialog-form-page.html',
})
export class DialogFormPage {


    protected readonly received = signal<FeedbackRequestDto | null>(null);


    private readonly dialog = inject(MatDialog);


    protected openFeedbackDialog(): void {

        const dialogRef = this.dialog.open(FeedbackDialog);

        dialogRef.componentInstance.onFormSubmission.subscribe(requestDto => {
            this.received.set(requestDto);
            dialogRef.close();
        });

    }


}
