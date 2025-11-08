package org.maiaframework.jwt

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class JwtAuthenticationToken : AbstractAuthenticationToken {


    val jwt: String
    private val principal: Any


    private constructor(
            userDetails: UserDetails,
            token: JwtAuthenticationToken
    ) : super(userDetails.authorities) {

        this.jwt = token.jwt
        this.principal = userDetails
        details = userDetails
        isAuthenticated = true

    }


    private constructor(jwt: String) : super(emptyList<GrantedAuthority>()) {

        this.jwt = jwt
        this.principal = jwt

    }


    override fun getCredentials(): Any? {

        return null

    }


    override fun getPrincipal(): Any {

        return this.principal

    }

    companion object {


        fun unauthenticatedFor(jwt: String): JwtAuthenticationToken {

            return JwtAuthenticationToken(jwt)

        }


        fun authenticatedFor(
                userDetails: UserDetails,
                unauthenticatedToken: JwtAuthenticationToken
        ): JwtAuthenticationToken {

            return JwtAuthenticationToken(userDetails, unauthenticatedToken)

        }

    }


}
