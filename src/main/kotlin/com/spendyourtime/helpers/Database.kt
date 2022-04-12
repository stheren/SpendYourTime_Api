package com.spendyourtime.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.spendyourtime.data.User
import org.slf4j.LoggerFactory
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
            val u = findUserByPseudo(pseudo) ?: throw Exception("Unknwon user")
            return u.password == Sha512.encode(pswText)
        }

        fun addUser(user: User) {
            allUsers.add(user)
        }
    }

    val logger = LoggerFactory.getLogger(this::class.java)
    val mapper = ObjectMapper()

    var allUsers = Users()

    fun loadFromJSON(){
        logger.info("LOAD_FROM_JSON")
        allUsers = mapper.readValue(Paths.get("users.json").toFile(), Users::class.java)

        Database.saveToJSON()
    }

    fun saveToJSON(){
        logger.info("SAVE_FROM_JSON")
        mapper.writeValue(Paths.get("users.json").toFile(), allUsers);
    }
}