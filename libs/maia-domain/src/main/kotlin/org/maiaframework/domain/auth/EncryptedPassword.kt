package org.maiaframework.domain.auth

import org.maiaframework.types.StringType

class EncryptedPassword(value: String) : StringType<EncryptedPassword>(value) {

    override fun toString(): String {
        return "MASKED"
    }

}
