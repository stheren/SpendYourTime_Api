package com.spendyourtime.helpers

import com.google.common.hash.Hashing
import com.spendyourtime.data.User
import java.nio.charset.StandardCharsets

object Database {
    var allUsers = arrayListOf<User>()

    fun findUserByPseudo(pseudo: String): User? {
        return allUsers.find { it.pseudo == pseudo }
    }

    fun findUserByEmail(email: String): User? {
        return allUsers.find { it.email == email }
    }

    fun checkPassword(pseudo: String, pswText: String): Boolean {
        var u = findUserByPseudo(pseudo)
        if (u == null)
            throw Exception("Unknwon user")
        return u.password.equals(Hashing.sha256().hashString(pswText, StandardCharsets.UTF_8).toString())
    }
}