package com.spendyourtime.helpers

import arrow.core.valid
import com.spendyourtime.data.Guild
import com.spendyourtime.data.User
import com.spendyourtime.data.Work
import org.ktorm.database.*
import org.ktorm.dsl.*
import org.ktorm.schema.*

object Users : Table<Nothing>("Users") {
    val user_id = int("user_id").primaryKey()
    var email = varchar("email")
    val pseudo = varchar("pseudo")
    val password = varchar("password")
}

object Guilds : Table<Nothing>("Guilds") {
    val guild_id = int("guild_id").primaryKey()
    val guild_name = varchar("guild_name")
    val owner = int("owner")
    val map_id = int("map_id")
    val work_name = varchar("work_name")
}


object DatabaseConnect {

    private val database = Database.connect(
        url = "jdbc:mysql://176.165.33.22:3306/SpendYourTime",
        driver = "com.mysql.jdbc.Driver",
        user = "SYTAPI",
        password = "SYTAPImdp"
    )

    class AllUsers : ArrayList<User>() {

        companion object {

            fun getAllUsers(): AllUsers {
                val users = AllUsers()
                database.from(Users).select().map {
                    users.add(User( it[Users.email]!!, it[Users.pseudo]!!, it[Users.password]!!))
                }
                return users
            }

            fun checkPassword(pseudo: String, pswText: String): Boolean {
                val u = findUserByPseudo(pseudo) ?: throw Exception("Unknwon user")
                return u.password == Sha512.encode(pswText)
            }

            fun findUserByEmail(email: String): User? {
                database.from(Users).select().where { (Users.email eq email) }.forEach {
                    if (it[Users.email] != null) {
                        return User(it[Users.email]!!, it[Users.pseudo]!!, it[Users.password]!!)
                    }
                }
                return null
            }

            fun findUserByPseudo(pseudo: String): User? {
                database.from(Users).select().where { (Users.pseudo eq pseudo) }.forEach {
                    if (it[Users.pseudo] != null) {
                        return User(it[Users.email]!!, it[Users.pseudo]!!, it[Users.password]!!)
                    }
                }
                return null
            }

            fun findUserById(id: Int): User? {
                database.from(Users).select().where { (Users.user_id eq id) }.forEach {
                    if (it[Users.user_id] != null) {
                        return User(it[Users.email]!!, it[Users.pseudo]!!, it[Users.password]!!)
                    }
                }
                return null
            }

            fun addUser(user: User) {
                database.insert(Users) {
                    set(it.email, user.email)
                    set(it.pseudo, user.pseudo)
                    set(it.password, Sha512.encode(user.password))
                    User.IncrementId()
                }
            }

//            fun removeUser(user: User) {
//                database.delete(Users) {
//                    it.user_id eq user.id
//                }
//            }
        }
    }


    class Guilds : ArrayList<Guild>() {

        companion object {

            fun findGuildByName(name: String): Guild? {
                database.from(com.spendyourtime.helpers.Guilds).select()
                    .where { (com.spendyourtime.helpers.Guilds.guild_name eq name) }.forEach {
                    if (it[com.spendyourtime.helpers.Guilds.guild_name] != null) {
                        return Guild(
                            it[com.spendyourtime.helpers.Guilds.guild_name]!!,
                            AllUsers.findUserById(it[com.spendyourtime.helpers.Guilds.owner]!!)!!,
                            Work.sendWork(it[com.spendyourtime.helpers.Guilds.work_name]!!)
                        )
                    }
                }
                return null
            }

//            fun addGuild(guild: Guild) {
//                database.insert(com.spendyourtime.helpers.Guilds) {
//                    set(it.guild_name, guild.name)
//                    set(it.owner, guild.owner.id)
//                    set(it.work_name, guild.typeWork)
//                    //manque le set de la map
//                }
//            }
//
//            fun findAllGuildsByOwner(owner: User): List<Guild> {
//                val guilds = ArrayList<Guild>()
//                database.from(com.spendyourtime.helpers.Guilds).select()
//                    .where { (com.spendyourtime.helpers.Guilds.owner eq owner.id) }.forEach {
//                    if (it[com.spendyourtime.helpers.Guilds.owner] != null) {
//                        guilds.add(
//                            Guild(
//                                it[com.spendyourtime.helpers.Guilds.guild_name]!!,
//                                AllUsers.findUserById(it[com.spendyourtime.helpers.Guilds.owner]!!)!!,
//                                Work.sendWork(it[com.spendyourtime.helpers.Guilds.work_name]!!)
//                            )
//                        )
//                    }
//                }
//                return guilds
//            }
        }
    }

//        fun findAllGuildByMember(userMember: User): List<Guild> {
//            return filter { it.employees.contains(userMember) }
//        }
//
//
//        fun findGuildById(id: Int): Guild? {
//            return find { it.id == id }
//        }
//    }
}


