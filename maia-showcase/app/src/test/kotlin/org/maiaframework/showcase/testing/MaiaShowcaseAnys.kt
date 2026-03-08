package org.maiaframework.showcase.testing

import org.maiaframework.domain.LifecycleState
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.showcase.user.UserEntity
import org.maiaframework.testing.domain.Anys
import java.time.Instant

object MaiaShowcaseAnys {


    val defaultUser = UserEntity(
        Instant.now(),
        "Some display Name",
        EmailAddress("nigel.nigelson@maiaframework.org"),
        "password",
        FirstName("Nigel"),
        Anys.defaultCreatedById,
        Instant.now(),
        LastName("Nigelson"),
        LifecycleState.ACTIVE,
        1L
    )


}
