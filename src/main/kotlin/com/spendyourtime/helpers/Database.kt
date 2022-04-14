package com.spendyourtime.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.spendyourtime.data.Guild
import com.spendyourtime.data.Message
import com.spendyourtime.data.User
import org.slf4j.LoggerFactory
import java.nio.file.Paths


object Database {
    class Users : ArrayList<User>(){
        fun findUserByPseudo(pseudo: String): User? {
            return find { it.pseudo == pseudo }
        }

        fun findUserByEmail(email: String): User? {
            return find { it.email == email }
        }

        fun checkPassword(pseudo: String, pswText: String): Boolean {
            val u = findUserByPseudo(pseudo) ?: throw Exception("Unknwon user")
            return u.password == Sha512.encode(pswText)
        }

        fun addUser(user: User) {
            allUsers.add(user)
            saveToJSON()
        }

        fun findUserById(id: Int): User? {
            return find { it.id == id }
        }

        fun removeUser(user: User) {
            allUsers.remove(user)
            saveToJSON()
        }
    }

    class Guilds : ArrayList<Guild>(){
        fun findGuildByName(name: String): Guild? {
            return find { it.name == name }
        }

        fun findAllGuildsByOwner(userOwner: User): List<Guild> {
            return filter { it.owner == userOwner.player }
        }

        fun findAllGuildByMember(userMember: User): List<Guild> {
            return filter { it.employees.contains(userMember.player) }
        }

        fun addGuild(guild: Guild) {
            add(guild)
            saveToJSON()
        }

        fun findGuildById(id: Int): Guild? {
            return find { it.id == id }
        }
    }

    class Chat : ArrayList<Message>(){
        fun addMessage(message: Message) {
            add(message)
            saveToJSON()
        }

        fun getMessagesBeetweenDate(dateStart: Long, dateEnd: Long): List<Message> {
            return filter { it.date in dateStart..dateEnd }
        }
    }

    val logger = LoggerFactory.getLogger(this::class.java)
    val mapper = ObjectMapper()

    var allUsers = Users()
    var allGuilds = Guilds()
    var allChat = Chat()

    fun loadFromJSON(){
        logger.info("LOAD_FROM_JSON")
        allUsers = mapper.readValue(Paths.get("users.json").toFile(), Users::class.java)
        allGuilds = mapper.readValue(Paths.get("guilds.json").toFile(), Guilds::class.java)
        allChat = mapper.readValue(Paths.get("chat.json").toFile(), Chat::class.java)
    }

    fun saveToJSON(){
        logger.info("SAVE_FROM_JSON")
        mapper.writeValue(Paths.get("users.json").toFile(), allUsers)
        mapper.writeValue(Paths.get("guilds.json").toFile(), allGuilds)
        mapper.writeValue(Paths.get("chat.json").toFile(), allChat)
    }
}