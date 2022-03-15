package com.spendyourtime.helpers

import arrow.core.handleError
import com.spendyourtime.data.User
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.JWTKeyID
import io.javalin.http.Context
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
        val e = JWT.decode(token).tap {
            it.claimValue("email").tap { email ->
                    p(User.findUserByEmail(email))
            }
        }
        e.handleError {
            throw Exception("JWT Decode has an error.")
        }
    }

    fun verification(ctx : Context, p : (User) -> Unit){
        find(ctx.header("token").toString()) { user ->
            if (user == null) {
                ctx.json("DECODED_BUT_UNKNOW_PLAYER")
                ctx.status(403)
            } else {
                p(user)
            }
        }
    }
}