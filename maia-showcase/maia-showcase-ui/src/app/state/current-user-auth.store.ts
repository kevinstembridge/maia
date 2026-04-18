import {signalStore, withComputed} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {CurrentUserStore} from '@app/gen-components/org/maiaframework/showcase/auth/current-user.store';
import {Authority} from '@app/gen-components/org/maiaframework/showcase/auth/Authority';


export const CurrentUserAuthStore = signalStore(


    { providedIn: 'root'},


    withComputed((store, currentUserStore = inject(CurrentUserStore)) => ({

        hasReadAuthority: computed<boolean>(() => {
            return currentUserStore.currentUser()?.grantedAuthorities.includes(Authority.READ) ?? false;
        }),

        hasSysOpsAuthority: computed<boolean>(() => {
            return currentUserStore.currentUser()?.grantedAuthorities.includes(Authority.SYS__OPS) ?? false;
        })

    })),


);
