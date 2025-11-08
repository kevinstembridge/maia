package org.maiaframework.jwt

import org.maiaframework.props.Props
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultClaims
import org.maiaframework.domain.contact.EmailAddress
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.util.Date

@Component
class JwtHelper(jwtProperties: JwtProperties, private val props: Props) {

    private val secret: ByteArray = jwtProperties.secret.toByteArray(Charset.forName("UTF-8"))
    private val defaultExpirationTimeInMinutes = jwtProperties.expirationTimeInMinutes


    fun createToken(emailAddress: EmailAddress, claims: Map<String, String> = emptyMap()): String {

        val expirationTimeInMinutes = this.props.getIntOrNull("maia.jwt.expirationTimeInMinutes") ?: this.defaultExpirationTimeInMinutes
        val expirationDate = Date(System.currentTimeMillis() + expirationTimeInMinutes * 60 * 1000)

        val defaultClaims = DefaultClaims()
        defaultClaims.subject = emailAddress.value
        defaultClaims.expiration = expirationDate

        defaultClaims.putAll(claims)

        return Jwts.builder()
                .setClaims(defaultClaims)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact()

    }


    @Throws(JwtExpiredException::class)
    fun parseToken(token: String): Jwt {

        try {

            val claims = Jwts.parser()
                    .setSigningKey(this.secret)
                    .parseClaimsJws(token)
                    .body

            return Jwt(claims)

        } catch (e: ExpiredJwtException) {
            throw JwtExpiredException(e.message ?: "Expired JWT Token")
        }

    }


    class Jwt(private val claims: Claims) {

        val username: String = this.claims.subject

    }


}
