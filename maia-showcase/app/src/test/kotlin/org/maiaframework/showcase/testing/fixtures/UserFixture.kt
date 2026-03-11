package org.maiaframework.showcase.testing.fixtures

import org.maiaframework.showcase.contact.EmailAddressEntity
import org.maiaframework.showcase.user.UserEntity


data class UserFixture(
    val userEntity: UserEntity,
    val emailAddressEntity: EmailAddressEntity,
    val rawPassword: String
) {


    val displayName = userEntity.displayName


}
