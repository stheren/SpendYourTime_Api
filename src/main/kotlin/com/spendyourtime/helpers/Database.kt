package com.spendyourtime.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.spendyourtime.data.User
import org.slf4j.LoggerFactory
import java.awt.print.Book
import java.nio.file.Paths


object Database {
    class Users : ArrayList<User>(){
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

    val logger = LoggerFactory.getLogger(this::class.java)
    val mapper = ObjectMapper()

    var allUsers = Users()

    fun loadFromJSON(){
        allUsers = mapper.readValue(Paths.get("users.json").toFile(), Users::class.java)
    }

    fun saveToJSON(){
        mapper.writeValue(Paths.get("users.json").toFile(), allUsers);
    }
}