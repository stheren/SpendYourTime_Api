package com.spendyourtime

import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.JWTKeyID
import io.github.nefilim.kjwt.jwtDecodeString
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object ServerTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val jwt = JWT.es256(JWTKeyID("kid-123")) {
            claim("name", "Juju773")
        }

        var e = jwt.encode()

        println(JWT.decode(e+"2"))
    }
}