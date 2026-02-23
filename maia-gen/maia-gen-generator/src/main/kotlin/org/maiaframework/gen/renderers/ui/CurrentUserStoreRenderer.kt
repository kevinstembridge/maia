package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef


class CurrentUserStoreRenderer(private val authoritiesDef: AuthoritiesDef) : AbstractTypescriptRenderer() {


    init {

        addImport(from = "@ngrx/signals", name = "patchState")
        addImport(from = "@ngrx/signals", name = "signalStore")
        addImport(from = "@ngrx/signals", name = "withComputed")
        addImport(from = "@ngrx/signals", name = "withMethods")
        addImport(from = "@ngrx/signals", name = "withState")
        addImport(from = "@angular/core", name = "computed")
        addImport(authoritiesDef.userSummaryDtoTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return authoritiesDef.currentUserStoreRenderedFilePath

    }


    override fun renderSourceBody() {

        append(
            """
            |
            |
            |type CurrentUserState = {
            |    currentUser: UserSummaryDto | null;
            |};
            |
            |
            |const initialState: CurrentUserState = {
            |    currentUser: null
            |};
            |
            |
            |export const CurrentUserStore = signalStore(
            |
            |
            |    { providedIn: 'root'},
            |
            |
            |    withState(initialState),
            |
            |
            |    withComputed(({ currentUser }) => ({
            |
            |        isSignedIn: computed<boolean>(() => {
            |            return !!currentUser();
            |        }),
            |
            |    })),
            |
            |
            |    withMethods((store) => ({
            |
            |        setCurrentUser(user: UserSummaryDto | null): void {
            |
            |            patchState(store, { currentUser: user });
            |
            |        },
            |
            |    })),
            |
            |
            |);""".trimMargin()
        )

    }


}
