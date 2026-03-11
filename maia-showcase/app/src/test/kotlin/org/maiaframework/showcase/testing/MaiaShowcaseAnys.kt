package org.maiaframework.showcase.testing

import org.maiaframework.domain.LifecycleState
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.showcase.user.UserEntity
import org.maiaframework.testing.domain.Anys
import java.time.Instant

object MaiaShowcaseAnys {


    val defaultUser = UserEntity(
        emptyList(),
        createdById = null,
        Instant.now(),
        "Some display Name",
        "password",
        FirstName("Nigel"),
        Anys.defaultCreatedById,
        lastModifiedById = null,
        Instant.now(),
        LastName("Nigelson"),
        LifecycleState.ACTIVE,
        1L
    )


}
