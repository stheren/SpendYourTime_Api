package com.spendyourtime.helpers

import com.spendyourtime.data.Skin
import com.spendyourtime.data.User
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.JWTKeyID

object Certification {
    fun create(u : User) : String{
        val jwt = JWT.es256(JWTKeyID("kid-123")) {
            claim("name", "Juju773")
        }
        return jwt.encode()
    }

    fun find(e : String, p : (User) -> Unit){
        println(JWT.decode(e).tap {
            println(it.claimValue("name"))
        })
        p(User("Email", "Pseudo", "OK", Skin()))
    }
}