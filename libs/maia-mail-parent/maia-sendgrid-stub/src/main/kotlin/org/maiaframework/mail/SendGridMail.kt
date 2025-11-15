package org.maiaframework.mail


/*
{
  "from": {
    "email": "ops@littleaircraft.com"
  },
  "subject": "New Aircraft Ownership Claim - N-10011",
  "personalizations": [
    {
      "to": [
        {
          "email": "ops@littleaircraft.com"
        }
      ],
      "cc": [
        {
          "email": "ops@littleaircraft.com"
        }
      ],
      "bcc": [
        {
          "email": "ops@littleaircraft.com"
        }
      ]
    }
  ],
  "content": [
    {
      "type": "text/html",
      "value": "<html>\n\n    <body>\n        <p>Hi null null,</p>\n        <p>We have received your ownership claim for aircraft TODO.</p>\n    </body>\n\n</html>"
    }
  ]
}
 */


data class SendGridMail(
    val from: Address,
    val subject: String,
    val personalizations: List<Personalization>,
    val content: List<Content>
)

data class Address(val email: String)

data class Personalization(val to: List<Address>?, val cc: List<Address>?, val bcc: List<Address>?)

data class Content(val type: String, val value: String)
