import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed} from '@angular/core';
import {UserSummaryDto} from '@app/gen-components/acme/auth/UserSummaryDto';


type CurrentUserState = {
    currentUser: UserSummaryDto | null;
};


const initialState: CurrentUserState = {
    currentUser: null
};


export const CurrentUserStore = signalStore(


    { providedIn: 'root'},


    withState(initialState),


    withComputed(({ currentUser }) => ({

        isSignedIn: computed<boolean>(() => {
            return !!currentUser();
        }),

    })),


    withMethods((store) => ({

        setCurrentUser(user: UserSummaryDto | null): void {

            patchState(store, { currentUser: user });

        },

    })),


);
