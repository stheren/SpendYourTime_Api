package com.spendyourtime

import com.spendyourtime.data.Guild
import com.spendyourtime.data.User
import com.spendyourtime.data.Work
import com.spendyourtime.helpers.Certification
import com.spendyourtime.helpers.Database
import com.spendyourtime.helpers.EmailValidator
import com.spendyourtime.helpers.retour
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.slf4j.LoggerFactory


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

            //USER ROUTES
            path("User") {
                post("register") { ctx ->
                    logger.info("USER_REGISTER")

                    //verif mail
                    if (ctx.formParam("email").isNullOrEmpty()) {
                        ctx.retour(400, "EMAIL_EMPTY")
                        return@post
                    }
                    if (!EmailValidator.isEmailValid(ctx.formParam("email").toString()) && !ctx.formParam("email")
                            .isNullOrEmpty()
                    ) {
                        ctx.retour(400, "EMAIL_INVALID")
                        return@post
                    }
                    if (Database.allUsers.findUserByEmail(
                            ctx.formParam("email").toString()
                        ) != null && !ctx.formParam("email").isNullOrEmpty()
                    ) {
                        ctx.retour(400, "EMAIL_ALREADY_EXIST")
                        return@post
                    }

                    //verif pseudo
                    if (ctx.formParam("pseudo").isNullOrEmpty()) {
                        ctx.retour(400, "PSEUDO_EMPTY")
                        return@post
                    }
                    if (ctx.formParam("pseudo").toString().length < 3 && !ctx.formParam("pseudo").isNullOrEmpty()) {
                        ctx.retour(400, "PSEUDO_TOO_SHORT")
                        return@post
                    }
                    if (ctx.formParam("pseudo").toString().length > 12) {
                        ctx.retour(400, "PSEUDO_TOO_LONG")
                        return@post
                    }
                    if (Database.allUsers.findUserByPseudo(
                            ctx.formParam("pseudo").toString()
                        ) != null && !ctx.formParam("pseudo").isNullOrEmpty()
                    ) {
                        ctx.retour(400, "PSEUDO_ALREADY_EXIST")
                        return@post
                    }

                    //verif password
                    if (ctx.formParam("password").isNullOrEmpty()) {
                        ctx.retour(400, "PASSWORD_EMPTY")
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

                post("login") { ctx ->
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
                        ) && !ctx.formParam("password").isNullOrEmpty()
                    ) {
                        ctx.retour(400, "PASSWORD_NOT_VALID")
                        return@post
                    }
                    val u = Database.allUsers.findUserByPseudo(ctx.formParam("pseudo").toString())!!
                    ctx.retour(200, Certification.create(u))
                }
            }

            //Player
            path("/Player") {
                post("position") { ctx ->
                    Certification.verification(ctx) {
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
                        ctx.retour(201, "POSITION_CHANGED")
                    }
                }

                get("skin") { ctx ->
                    logger.info("GET_SKIN")
                    Certification.verification(ctx) { user ->
                        ctx.retour(200, user.player.skin)
                    }

                }

                app.put("skin") { ctx ->
                    logger.info("CHANGE_SKIN_PLAYER")
                    Certification.verification(ctx) { user ->
                        user.player.skin.body = ctx.formParam("body")?.toInt() ?: user.player.skin.body
                        user.player.skin.eyes = ctx.formParam("eyes")?.toInt() ?: user.player.skin.eyes
                        user.player.skin.accessories =
                            ctx.formParam("accessories")?.toInt() ?: user.player.skin.accessories
                        user.player.skin.hairstyle = ctx.formParam("hairstyle")?.toInt() ?: user.player.skin.hairstyle
                        user.player.skin.outfit = ctx.formParam("outfit")?.toInt() ?: user.player.skin.outfit
                        ctx.retour(200, user.player.skin)
                    }
                }


                post("joinGuild") { ctx ->
                    Certification.verification(ctx) { user ->
                        val guild = ctx.formParam("guild")?.toString() ?: ""

                        logger.info("PlayerJoinGuild")
                        if (ctx.formParam("guild").isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_EMPTY")
                            return@verification
                        }

                        if (Database.allGuilds.findGuildByName(guild) == null) {
                            ctx.retour(400, "GUILD_NOT_FOUND")
                            return@verification
                        }
                        Database.allGuilds.findGuildByName(guild)?.AddMember(user.player)
                        ctx.retour(200, "SUCCESS_JOIN_GUILD")
                    }
                }

                post("/leaveGuild") { ctx ->
                    Certification.verification(ctx) { user ->
                        val guild = ctx.formParam("guild")?.toString() ?: ""

                        logger.info("PlayerLeaveGuild")
                        if (ctx.formParam("guild").isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_EMPTY")
                            return@verification
                        }

                        if (Database.allGuilds.findGuildByName(guild) == null) {
                            ctx.retour(400, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        Database.allGuilds.findGuildByName(guild)?.RemoveMember(user.player)
                        ctx.retour(200, "SUCCESS_LEAVE_GUILD")
                    }
                }

                post("/createGuild") { ctx ->
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

                get("/ownerGuild") { ctx ->
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
                get("allGuild") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_GUILD")
                        ctx.retour(200, Database.allGuilds)
                    }
                }

                get("waitingList") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_WAITING_LIST")
                        val guild = ctx.formParam("guild")?.toString() ?: ""
                        if (guild.isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildByName(guild)
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

                post("acceptMember") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("POST_ACCEPT_MEMBER")
                        val guild = ctx.formParam("guild")?.toString() ?: ""
                        val player = ctx.formParam("playerName")?.toString() ?: ""
                        if (guild.isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        if (player.isNullOrEmpty()) {
                            ctx.retour(400, "PLAYER_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildByName(guild)
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        if (Database.allUsers.findUserByPseudo(player) == null) {
                            ctx.retour(404, "PLAYER_NOT_EXIST")
                            return@verification
                        }
                        if (!dataguild.waitingList.contains(Database.allUsers.findUserByPseudo(player)!!.player)) {
                            ctx.retour(404, "PLAYER_NOT_WAITING_LIST")
                            return@verification
                        }
                        dataguild.AddMember(Database.allUsers.findUserByPseudo(player)!!.player)
                        dataguild.waitingList.remove(Database.allUsers.findUserByPseudo(player)!!.player)
                        ctx.retour(200, "SUCCESS_ACCEPT_MEMBER")
                    }
                }

                post("refuseMember") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("POST_REFUSE_MEMBER")
                        val guild = ctx.formParam("guild")?.toString() ?: ""
                        val player = ctx.formParam("playerName")?.toString() ?: ""
                        if (guild.isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        if (player.isNullOrEmpty()) {
                            ctx.retour(400, "PLAYER_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildByName(guild)
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        if (Database.allUsers.findUserByPseudo(player) == null) {
                            ctx.retour(404, "PLAYER_NOT_EXIST")
                            return@verification
                        }
                        if (!dataguild.waitingList.contains(Database.allUsers.findUserByPseudo(player)!!.player)) {
                            ctx.retour(404, "PLAYER_NOT_WAITING_LIST")
                            return@verification
                        }
                        dataguild.waitingList.remove(Database.allUsers.findUserByPseudo(player)!!.player)
                        ctx.retour(200, "SUCCESS_REFUSE_MEMBER")
                    }
                }

                post("removeMember") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("POST_REMOVE_MEMBER")
                        val guild = ctx.formParam("guild")?.toString() ?: ""
                        val player = ctx.formParam("playerName")?.toString() ?: ""
                        if (guild.isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        if (player.isNullOrEmpty()) {
                            ctx.retour(400, "PLAYER_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildByName(guild)
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        if (Database.allUsers.findUserByPseudo(player) == null) {
                            ctx.retour(404, "PLAYER_NOT_EXIST")
                            return@verification
                        }
                        if (!dataguild.employees.contains(Database.allUsers.findUserByPseudo(player)!!.player)) {
                            ctx.retour(404, "PLAYER_NOT_MEMBER")
                            return@verification
                        }
                        dataguild.RemoveMember(Database.allUsers.findUserByPseudo(player)!!.player)
                        ctx.retour(200, "SUCCESS_REMOVE_MEMBER")
                    }
                }

                get("allMembers") { ctx ->
                    Certification.verification(ctx) { user ->
                        logger.info("GET_ALL_MEMBERS")
                        val guild = ctx.formParam("guild")?.toString() ?: ""
                        if (guild.isNullOrEmpty()) {
                            ctx.retour(400, "GUILD_REQUIRED")
                            return@verification
                        }
                        val dataguild = Database.allGuilds.findGuildByName(guild)
                        if (dataguild == null) {
                            ctx.retour(404, "GUILD_NOT_EXIST")
                            return@verification
                        }
                        if (dataguild.owner != user.player) {
                            ctx.retour(403, "PLAYER_NOT_OWNER_GUILD")
                            return@verification
                        }
                        ctx.retour(200, dataguild.employees)
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