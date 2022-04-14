package com.spendyourtime

import com.spendyourtime.data.Guild
import com.spendyourtime.data.Message
import com.spendyourtime.data.User
import com.spendyourtime.data.Work
import com.spendyourtime.helpers.Certification
import com.spendyourtime.helpers.Database
import com.spendyourtime.helpers.EmailValidator
import com.spendyourtime.helpers.retour
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.slf4j.LoggerFactory
import java.util.*


object Server {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = LoggerFactory.getLogger(this::class.java)
        Database.loadFromJSON()

        val app = Javalin.create().apply {
            exception(Exception::class.java) { e, ctx ->
                ctx.json(e.message.toString())
                ctx.status(403)
            }
            this.error(404) { ctx ->
                ctx.json("Error")
            }
        }.start(7000)
        app.routes {

            //LOGIN
            app.post("/login") { ctx ->
                logger.info("POST_LOGIN")

                //Check pseudo login
                if (ctx.formParam("pseudo").isNullOrEmpty()) {
                    ctx.retour(400, "PSEUDO_IS_EMPTY")
                    return@post
                }
                if (Database.allUsers.findUserByPseudo(ctx.formParam("pseudo").toString()) == null) {
                    ctx.retour(400, "PSEUDO_NOT_EXIST")
                    return@post
                }

                //check Password login
                if (ctx.formParam("password").isNullOrEmpty()) {
                    ctx.retour(400, "PASSWORD_IS_EMPTY")
                    return@post
                }
                if (!Database.allUsers.checkPassword(
                        ctx.formParam("pseudo").toString(), ctx.formParam("password").toString()
                    )
                ) {
                    ctx.retour(400, "PASSWORD_NOT_VALID")
                    return@post
                }
                val u = Database.allUsers.findUserByPseudo(ctx.formParam("pseudo").toString())!!
                ctx.retour(200, Certification.create(u))
            }

            //REGISTER
            app.post("/register") { ctx ->
                logger.info("USER_REGISTER")

                //verif mail
                if (ctx.formParam("email").isNullOrEmpty()) {
                    ctx.retour(400, "EMAIL_EMPTY")
                    return@post
                }
                if (!EmailValidator.isEmailValid(ctx.formParam("email").toString())) {
                    ctx.retour(400, "EMAIL_INVALID")
                    return@post
                }
                if (Database.allUsers.findUserByEmail(ctx.formParam("email").toString()) != null) {
                    ctx.retour(400, "EMAIL_ALREADY_EXIST")
                    return@post
                }

                //verif pseudo
                if (ctx.formParam("pseudo").isNullOrEmpty()) {
                    ctx.retour(400, "PSEUDO_EMPTY")
                    return@post
                }
                if (ctx.formParam("pseudo").toString().length < 3) {
                    ctx.retour(400, "PSEUDO_TOO_SHORT")
                    return@post
                }
                if (ctx.formParam("pseudo").toString().length > 12) {
                    ctx.retour(400, "PSEUDO_TOO_LONG")
                    return@post
                }
                if (Database.allUsers.findUserByPseudo(
                        ctx.formParam("pseudo").toString()
                    ) != null
                ) {
                    ctx.retour(400, "PSEUDO_ALREADY_EXIST")
                    return@post
                }

                //verif password
                if (ctx.formParam("password").isNullOrEmpty()) {
                    ctx.retour(400, "PASSWORD_EMPTY")
                    return@post
                }

                if (ctx.formParam("password").toString().length < 2) {
                    ctx.retour(400, "PASSWORD_TOO_SHORT")
                    return@post
                }

                val u: User = User(
                    ctx.formParam("email").toString(),
                    ctx.formParam("pseudo").toString(),
                    ctx.formParam("password").toString()
                )
                Database.allUsers.addUser(u)
                ctx.retour(201, Certification.create(u))
            }

            //USER ROUTES
            path("User") {
                get("/") { ctx ->
                    Certification.verification(ctx) { user ->
                        ctx.retour(200, user)
                    }
                }

                get("/{id}") { ctx ->
                    Certification.verification(ctx) {
                        val datauser = Database.allUsers.findUserById(ctx.pathParam("id").toInt())
                        if (datauser == null) {
                            ctx.retour(404, "USER_NOT_FOUND")
                            return@verification
                        }
                        ctx.retour(200, datauser)
                    }
                }

                //modify user pseudo, email or password
                put("/") { ctx ->
                    Certification.verification(ctx) { user ->
                        var email = ctx.formParam("email").toString()
                        var pseudo = ctx.formParam("pseudo").toString()
                        var password = ctx.formParam("password").toString()


                        if (pseudo.isEmpty()) {
                            pseudo = user.pseudo
                        } else {
                            if (pseudo.length < 3) {
                                ctx.retour(400, "PSEUDO_TOO_SHORT")
                                return@verification
                            }
                            if (pseudo.length > 12) {
                                ctx.retour(400, "PSEUDO_TOO_LONG")
                                return@verification
                            }
                            if (Database.allUsers.findUserByPseudo(pseudo) != null) {
                                ctx.retour(400, "PSEUDO_ALREADY_EXIST")
                                return@verification
                            }
                        }

                        if (email.isEmpty()) {
                            email = user.email
                        } else {
                            if (!EmailValidator.isEmailValid(email)) {
                                ctx.retour(400, "EMAIL_INVALID")
                                return@verification
                            }
                            if (Database.allUsers.findUserByEmail(email) != null) {
                                ctx.retour(400, "EMAIL_ALREADY_EXIST")
                                return@verification
                            }
                        }

                        if (password.isEmpty()) {
                            password = user.password
                        } else {
                            if (password.length < 2) {
                                ctx.retour(400, "PASSWORD_TOO_SHORT")
                                return@verification
                            }
                        }
                        user.pseudo = pseudo
                        user.email = email
                        user.password = password
                        Database.saveToJSON()
                        ctx.retour(200, Certification.create(user))
                    }

                }

                delete("/") { ctx ->
                    Certification.verification(ctx) { user ->
                        Database.allUsers.removeUser(user)
                        Database.saveToJSON()
                        ctx.retour(200, "USER_DELETED")
                    }
                }
            }


            //Player
            path("/Player") {
                put("/position") { ctx ->
                    Certification.verification(ctx) { user ->
                        val x = ctx.formParam("posX")?.toInt() ?: -1
                        val y = ctx.formParam("posY")?.toInt() ?: -1

                        logger.info("PlayerPosition")
                        if (ctx.formParam("posX").isNullOrEmpty()) {
                            ctx.retour(400, "POSX_IS_EMPTY")
                            return@verification
                        }
                        if (x < 0 || x > 100 && !ctx.formParam("posX").isNullOrEmpty()) {
                            ctx.retour(400, "POSX_IS_NOT_VALID")
                            return@verification
                        }
                        if (ctx.formParam("posY").isNullOrEmpty()) {
                            ctx.retour(400, "POSY_IS_EMPTY")
                            return@verification
                        }
                        if (y < 0 || y > 100 && !ctx.formParam("posY").isNullOrEmpty()) {
                            ctx.retour(400, "POSY_IS_NOT_VALID")
                            return@verification
                        }
                        user.player.position.x = x
                        user.player.position.y = y
                        ctx.retour(201, "POSITION_CHANGED")
                    }
                }

                get("/skin") { ctx ->
                    logger.info("GET_SKIN")
                    Certification.verification(ctx) { user ->
                        ctx.retour(200, user.player.skin)
                    }

                }

                put("/skin") { ctx ->
                    logger.info("CHANGE_SKIN_PLAYER")
                    Certification.verification(ctx) { user ->
                        user.player.skin.body = ctx.formParam("body")?.toInt() ?: user.player.skin.body
                        user.player.skin.eyes = ctx.formParam("eyes")?.toInt() ?: user.player.skin.eyes
                        user.player.skin.accessories =
                            ctx.formParam("accessories")?.toInt() ?: user.player.skin.accessories
                        user.player.skin.hairstyle =
                            ctx.formParam("hairstyle")?.toInt() ?: user.player.skin.hairstyle
                        user.player.skin.outfit = ctx.formParam("outfit")?.toInt() ?: user.player.skin.outfit
                        ctx.retour(200, user.player.skin)
                    }
                }

                get("/guilds") { ctx ->
                    Certification.verification(ctx) { user ->
                        if (Database.allGuilds.findAllGuildByMember(user).isEmpty()) {
                            ctx.retour(200, "NO_GUILD")
                            return@verification
                        } else {
                            ctx.retour(200, Database.allGuilds.findAllGuildByMember(user))
                        }
                    }
                }

                get("/owns") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_OWNER_GUILD")
                        if (Database.allGuilds.findAllGuildsByOwner(user).isEmpty()) {
                            ctx.retour(400, "NO_OWNER_GUILD")
                        } else {
                            ctx.retour(200, Database.allGuilds.findAllGuildsByOwner(user))
                        }
                    }
                }
            }


            //Guilde
            path("Guild") {
                get("/") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_GUILD")
                        ctx.retour(200, Database.allGuilds)
                    }
                }

                post("/") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("POST_GUILD")
                        if (ctx.formParam("nameGuild").isNullOrEmpty()) {
                            ctx.retour(400, "NAME_GUILD_REQUIRED")
                            return@verification
                        }
                        if (ctx.formParam("typeWork").isNullOrEmpty()) {
                            ctx.retour(400, "TYPE_WORK_REQUIRED")
                        }
                        if (!Work.validateWork(ctx.formParam("typeWork").toString())) {
                            ctx.retour(400, "TYPE_WORK_NOT_EXIST")
                        }
                        Guild(
                            ctx.formParam("nameGuild").toString(),
                            user.player,
                            Work.sendWork(ctx.formParam("typeWork").toString())
                        )
                        ctx.retour(200, "SUCCESS_CREATE_GUILD")
                    }
                }

                get("/{id}") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_GUILD")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            return@verification
                        }
                        if (Database.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(400, "GUILD_NOT_FOUND")
                            return@verification
                        }
                        ctx.retour(200, Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())!!)
                    }
                }

                put("/{id}") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("PUT_GUILD")
                        var name = ctx.formParam("nameGuild") ?: ""
                        var work = ctx.formParam("typeWork") ?: ""

                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            return@verification
                        }
                        val guild = Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (guild == null) {
                            ctx.retour(400, "GUILD_NOT_FOUND")
                            return@verification
                        }

                        if (name.isEmpty()) {
                            name = guild.name
                        } else {
                            if (Database.allGuilds.findGuildByName(name) != null) {
                                ctx.retour(400, "GUILD_ALREADY_EXIST")
                                return@verification
                            }
                        }

                        if (work.isEmpty()) {
                            work = guild.typeWork.name
                        } else {
                            if (!Work.validateWork(work)) {
                                ctx.retour(400, "TYPE_WORK_NOT_EXIST")
                                return@verification
                            }
                        }
                        guild.name = name
                        guild.typeWork = Work.sendWork(work)
                        ctx.retour(200, "SUCCESS_UPDATE_GUILD")
                    }
                }

                delete("/{id}") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("DELETE_GUILD")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "ID_GUILD_REQUIRED")
                            return@verification
                        }
                        if (Database.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(400, "GUILD_NOT_FOUND")
                            return@verification
                        }
                        if (!Database.allGuilds.findAllGuildsByOwner(user)
                                .contains(Database.allGuilds.findGuildById(ctx.pathParam("id").toInt()))
                        ) {
                            ctx.retour(400, "GUILD_NOT_OWNER")
                        }
                        Database.allGuilds.remove(Database.allGuilds.findGuildById(ctx.pathParam("id").toInt()))
                        ctx.retour(200, "SUCCESS_DELETE_GUILD")
                    }
                }

                patch("/{id}/join") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("PlayerJoinGuild")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_EMPTY")
                            return@verification
                        }

                        if (Database.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(400, "GUILD_NOT_FOUND")
                            return@verification
                        }
                        Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())?.AddMember(user.player)
                        ctx.retour(200, "SUCCESS_JOIN_WAITING_LIST")
                    }
                }

                patch("/{id}/leave") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("PlayerLeaveGuild")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_EMPTY")
                            return@verification
                        }

                        if (Database.allGuilds.findGuildById(ctx.pathParam("id").toInt()) == null) {
                            ctx.retour(400, "GUILD_NOT_FOUND")
                            return@verification
                        }
                        if (Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                                ?.RemoveMember(user.player) != true
                        ) {
                            ctx.retour(400, "PLAYER_NOT_IN_GUILD_OR_IS_OWNER")
                        }
                        ctx.retour(200, "SUCCESS_LEAVE_WAITING_LIST")
                    }
                }

                get("/{id}/owner") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("OwnerGuild")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_EMPTY")
                            return@verification
                        }
                        Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())?.let { ctx.retour(200, it.owner) }
                    }
                }

                get("/{id}/members") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_MEMBERS")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        ctx.retour(200, dataguild.employees)
                    }
                }

                get("/{id}/waiting") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_WAITING_LIST")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        ctx.retour(200, dataguild.waitingList)
                    }
                }

                patch("/{id}/accept/{playerId}") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("ACCEPT_MEMBER")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        if (ctx.pathParam("playerId").isEmpty()) {
                            ctx.retour(400, "PLAYER_REQUIRED")
                            return@verification
                        }
                        val datauser = Database.allUsers.findUserById(ctx.pathParam("playerId").toInt())
                        if (datauser == null) {
                            ctx.retour(400, "PLAYER_NOT_FOUND")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        if (!dataguild.waitingList.contains(datauser.player)) {
                            ctx.retour(404, "PLAYER_NOT_WAITING_LIST")
                            return@verification
                        }
                        dataguild.AddMember(datauser.player)
                        dataguild.waitingList.remove(datauser.player)
                        ctx.retour(200, "SUCCESS_ACCEPT_MEMBER")
                    }
                }

                patch("/{id}/decline/{playerId}") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("REFUSE_MEMBER")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        if (ctx.pathParam("playerId").isEmpty()) {
                            ctx.retour(400, "PLAYER_REQUIRED")
                            return@verification
                        }
                        val datauser = Database.allUsers.findUserById(ctx.pathParam("playerId").toInt())
                        if (datauser == null) {
                            ctx.retour(400, "PLAYER_NOT_FOUND")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        if (!dataguild.waitingList.contains(datauser.player)) {
                            ctx.retour(404, "PLAYER_NOT_WAITING_LIST")
                            return@verification
                        }
                        dataguild.waitingList.remove(datauser.player)
                        ctx.retour(200, "SUCCESS_REFUSE_MEMBER")
                    }
                }

                patch("/{id}/kick/{playerId}") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("KICK_MEMBER")
                        if (ctx.pathParam("id").isEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        if (ctx.pathParam("playerId").isEmpty()) {
                            ctx.retour(400, "PLAYER_REQUIRED")
                            return@verification
                        }
                        val datauser = Database.allUsers.findUserById(ctx.pathParam("playerId").toInt())
                        if (datauser == null) {
                            ctx.retour(400, "PLAYER_NOT_FOUND")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildById(ctx.pathParam("id").toInt())
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        if (!dataguild.employees.contains(datauser.player)) {
                            ctx.retour(404, "PLAYER_NOT_MEMBER_GUILD")
                        }
                        dataguild.employees.remove(datauser.player)
                        ctx.retour(200, "SUCCESS_KICK_MEMBER")
                    }
                }

            }

            //Chat
            path("/Chat"){
                get("/"){ ctx -> //List last message from 5 minutes
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_CHAT")
                        ctx.retour(200, Database.allChat.getMessagesBeetweenDate(Date().time - 300000, Date().time))
                    }
                }

                post("/create"){ ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("CREATE_MESSAGE_CHAT")
                        val message = ctx.formParam("message")
                        if (message.isNullOrEmpty()) {
                            ctx.retour(400, "MESSAGE_REQUIRED")
                            return@verification
                        }
                        Database.allChat.addMessage(Message(message, user, Date().time))
                        ctx.retour(200, "SUCCESS_CREATE_MESSAGE_CHAT")
                    }
                }
            }


            //MAP
            app.get("map") { ctx ->
                logger.info("RECUP_MAP")
                ctx.status(501)
            }
        }
    }
}