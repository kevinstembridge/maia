package org.maiaframework.mail


data class SendGridMail(
    val from: Address,
    val subject: String,
    val personalizations: List<Personalization>,
    val content: List<Content>
)


data class Address(val email: String)


data class Personalization(val to: List<Address>?, val cc: List<Address>?, val bcc: List<Address>?)


data class Content(val type: String, val value: String)

