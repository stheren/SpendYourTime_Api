package com.spendyourtime.helpers

import arrow.core.some
import com.spendyourtime.data.User
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.JWTKeyID
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object Certification {


    fun create(u : User) : String{
        val jwt = JWT.es256(JWTKeyID("kid-123")) {
            subject("Token ID")
            claim("email", u.email)
            issuedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(1516239022), ZoneId.of("UTC")))
        }
        return jwt.encode()
    }

    fun find(token : String, p : (User?) -> Unit) {
        JWT.decode(token).tap {
            it.claimValue("email").tap { email ->
                    p(User.findUserByEmail(email))
            }
        }
    }
}