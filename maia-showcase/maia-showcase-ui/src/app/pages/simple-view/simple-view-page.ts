import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {SimpleEntityDetailView} from '@app/gen-components/org/maiaframework/showcase/simple/simple-entity-detail-view';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth-service';
import {Authority} from '@app/gen-components/org/maiaframework/showcase/auth/Authority';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout, SimpleEntityDetailView, MatButtonModule, MatIconModule],
    selector: 'app-simple-view',
    templateUrl: './simple-view-page.html',
})
export class SimpleViewPage {


    private readonly route = inject(ActivatedRoute);


    private readonly router = inject(Router);


    private readonly authService = inject(AuthService);


    protected readonly entityId = toSignal(
        this.route.paramMap.pipe(
            map(p => p.get('id'))
        )
    );


    protected get canEdit(): boolean {
        return this.authService.currentUserHasThisAuthority(Authority.WRITE);
    }


    onEditClicked(): void {
        const id = this.entityId();
        if (id) {
            this.router.navigate(['/simple/edit', id]);
        }
    }


}
