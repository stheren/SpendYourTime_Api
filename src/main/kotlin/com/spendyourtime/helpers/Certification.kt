package com.spendyourtime.helpers

import arrow.core.handleError
import com.spendyourtime.data.User
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.JWTKeyID
import io.javalin.http.Context
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object Certification {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun create(u : User) : String{
        val jwt = JWT.es256(JWTKeyID("kid-123")) {
            subject("Token ID")
            claim("email", u.email)
            issuedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(1516239022), ZoneId.of("UTC")))
        }
        logger.info("Token is create for ${u.pseudo}")
        return jwt.encode()
    }

    fun find(ctx : Context, token : String, p : (User?) -> Unit) {
        val e = JWT.decode(token).tap {
            it.claimValue("email").tap { email ->
                    p(Database.allUsers.findUserByEmail(email))
            }
        }
        e.handleError {
            logger.info("UNDECODED_JWT_TOKEN")
            ctx.json("DECODED_BUT_UNKNOW_PLAYER")
            ctx.status(403)
        }
    }

    fun verification(ctx : Context, p : (User) -> Unit){
        find(ctx, ctx.header("token").toString()) { user ->
            if (user == null) {
                logger.info("DECODED_BUT_UNKNOW_PLAYER")
                ctx.json("DECODED_BUT_UNKNOW_PLAYER")
                ctx.status(403)
            } else {
                p(user)
            }
        }
    }
}