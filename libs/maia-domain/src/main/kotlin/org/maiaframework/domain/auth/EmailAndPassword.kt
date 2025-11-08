package org.maiaframework.domain.auth

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.maiaframework.domain.contact.EmailAddress

@JsonIgnoreProperties(ignoreUnknown = true)
class EmailAndPassword
@JsonCreator constructor(
    @param:JsonProperty("emailAddress") val emailAddressRaw: String,
    @param:JsonProperty("password") val password: String
) {


    val emailAddressObj: EmailAddress = EmailAddress(this.emailAddressRaw)


    override fun toString(): String {
        return "EmailAndPassword(emailAddress='$emailAddressRaw', password='MASKED')"
    }


}
