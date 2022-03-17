package com.spendyourtime.helpers

import com.spendyourtime.data.User

object Database {
    var allUsers = arrayListOf<User>()

    fun findUserByPseudo(pseudo: String): User? {
        return allUsers.find { it.pseudo == pseudo }
    }

    fun findUserByEmail(email: String): User? {
        return allUsers.find { it.email == email }
    }

    fun checkPassword(pseudo: String, pswText: String): Boolean {
        var u = findUserByPseudo(pseudo) ?: throw Exception("Unknwon user")
        return u.password == Sha512.encode(pswText)
    }
}