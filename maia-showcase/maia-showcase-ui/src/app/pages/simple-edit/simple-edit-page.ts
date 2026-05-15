import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {SimpleEntityEditForm} from './simple-entity-edit-form';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout, SimpleEntityEditForm],
    selector: 'app-simple-edit',
    templateUrl: './simple-edit-page.html',
})
export class SimpleEditPage {


    private readonly route = inject(ActivatedRoute);


    private readonly router = inject(Router);


    protected readonly entityId = toSignal(
        this.route.paramMap.pipe(
            map(p => p.get('id'))
        )
    );


    onSaveClicked(): void {
        this.router.navigate(['/simple/view', this.entityId()]);
    }


    onCancelClicked(): void {
        this.router.navigate(['/simple/view', this.entityId()]);
    }


}
