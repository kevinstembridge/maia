import {signalStore, withComputed} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {CurrentUserStore} from '@app/gen-components/org/maiaframework/showcase/current-user-store';
import {Authority} from '@app/gen-components/org/maiaframework/showcase/Authority';


export const CurrentUserAuthStore = signalStore(


    { providedIn: 'root'},


    withComputed((store, currentUserStore = inject(CurrentUserStore)) => ({

        hasReadAuthority: computed<boolean>(() => {
            return currentUserStore.currentUser()?.grantedAuthorities.includes(Authority.READ) ?? false;
        }),

        hasWriteAuthority: computed<boolean>(() => {
            return currentUserStore.currentUser()?.grantedAuthorities.includes(Authority.WRITE) ?? false;
        })

    })),


);
