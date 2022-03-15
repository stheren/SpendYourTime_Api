package com.spendyourtime.helpers

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

    fun find(token : String, p : (User) -> Unit){
        println(JWT.decode(token).tap {
            println(it.claimValue("email"))
            p(User.findUserByPseudo(it.claimValue("email").toString()) ?: throw Exception("User don't exist !"))
        })
    }
}