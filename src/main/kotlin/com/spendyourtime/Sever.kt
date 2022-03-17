package com.spendyourtime

import com.spendyourtime.data.Guild
import com.spendyourtime.data.User
import com.spendyourtime.helpers.Certification
import com.spendyourtime.helpers.Database
import com.spendyourtime.helpers.EmailValidator
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.slf4j.LoggerFactory
import javax.xml.crypto.Data


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
            app.get("/ping") { ctx ->
                ctx.json("Pong")
            } //c'est rigolo

            //USER REGISTER + LOGIN
            path("User") {
                post("register") { ctx ->
                    logger.info("USER_REGISTER")
                    var errors = arrayListOf<String>()

                    //verif mail
                    if (ctx.formParam("email").isNullOrEmpty()) {
                        errors.add("EMAIL_IS_EMPTY")
                        logger.info("EMAIL_IS_EMPTY")
                    }
                    if (!EmailValidator.isEmailValid(ctx.formParam("email").toString())) {
                        errors.add("EMAIL_NOT_VALID")
                        logger.info("EMAIL_NOT_VALID")
                    }
                    if (Database.allUsers.findUserByEmail(ctx.formParam("email").toString()) != null) {
                        errors.add("EMAIL_ALREADY_USE")
                        logger.info("EMAIL_ALREADY_USE")
                    }

                    //verif pseudo
                    if (ctx.formParam("pseudo").isNullOrEmpty()) {
                        errors.add("PSEUDO_IS_EMPTY")
                        logger.info("PSEUDO_IS_EMPTY")
                    }
                    if (ctx.formParam("pseudo").toString().length < 3) {
                        errors.add("PSEUDO_TOO_SHORT")
                        logger.info("PSEUDO_TOO_SHORT")
                    }
                    if (ctx.formParam("pseudo").toString().length > 12) {
                        errors.add("PSEUDO_TOO_LONG")
                        logger.info("PSEUDO_TOO_LONG")
                    }
                    if (Database.allUsers.findUserByPseudo(ctx.formParam("pseudo").toString()) != null) {
                        errors.add("PSEUDO_ALREADY_USE")
                        logger.info("PSEUDO_ALREADY_USE")
                    }

                    //verif password
                    if (ctx.formParam("password").isNullOrEmpty()) {
                        errors.add("PASSWORD_IS_EMPTY")
                        logger.info("PASSWORD_IS_EMPTY")
                    }


                    if (errors.isNotEmpty()) {
                        ctx.status(400).json(errors)
                        logger.info("FAILED_OF_REGISTER")
                        logger.info("END_OF_REGISTER")
                    } else {
                        var u: User = User(
                            ctx.formParam("email").toString(),
                            ctx.formParam("pseudo").toString(),
                            ctx.formParam("password").toString()
                        )
                        ctx.status(201)
                        ctx.json(Certification.create(u))
                        logger.info("SUCCESS_REGISTER")
                        logger.info("END_OF_REGISTER")
                    }
                }

                post("login") { ctx ->
                    logger.info("POST_LOGIN")
                    var errors = arrayListOf<String>()

                    //Check pseudo login
                    if (ctx.formParam("pseudo").isNullOrEmpty()) {
                        errors.add("PSEUDO_IS_EMPTY")
                        logger.info("PSEUDO_IS_EMPTY")
                    }
                    if (Database.allUsers.findUserByPseudo(ctx.formParam("pseudo").toString()) != null) {
                        errors.add("PSEUDO_UNKNOW")
                        logger.info("PSEUDO_UNKNOW")
                    }

                    //check Password login
                    if (ctx.formParam("password").isNullOrEmpty()) {
                        errors.add("PASSWORD_IS_EMPTY")
                        logger.info("PASSWORD_IS_EMPTY")
                    }
                    if (!Database.allUsers.checkPassword(ctx.formParam("pseudo").toString(), ctx.formParam("password").toString())) {
                        errors.add("PASSWORD_INCORRECT")
                        logger.info("PASSWORD_INCORRECT")
                    }

                    if (errors.isNotEmpty()) {
                        ctx.status(400).json(errors)
                        logger.info("FAILED_LOGIN_IN")
                        logger.info("END_OF_LOGIN")
                    } else {
                        val u = Database.allUsers.findUserByPseudo(ctx.formParam("pseudo").toString())!!
                        ctx.json(Certification.create(u))
                        ctx.json("Success_Login_in")
                        ctx.status(202)
                    }
                }

                get("skin") { ctx ->
                    logger.info("GET_SKIN")
                    Certification.verification(ctx) { user ->
                        ctx.json(user.player.skin)
                        ctx.status(200)
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
                        ctx.status(201)
                        ctx.json("CHANGED_SKIN")
                    }
                }
            }

            //Player
            app.post("/Player/position") { ctx ->
                var errors = arrayListOf<String>()
                val x = ctx.formParam("posX")?.toInt() ?: -1
                val y = ctx.formParam("posY")?.toInt() ?: -1

                logger.info("PlayerPosition")
                if (ctx.formParam("posX").isNullOrEmpty()) {
                    errors.add("POSITION_X_ERROR")
                    logger.info("POSITION_X_ERROR")
                }
                if (x < 0 || x > 100) {
                    errors.add("POSITION_X_NOT_IN_MAP")
                    logger.info("POSITION_X_NOT_IN_MAP")
                }
                if (ctx.formParam("posY").isNullOrEmpty()) {
                    errors.add("POSITION_Y_ERROR")
                    logger.info("POSITION_Y_ERROR")
                }
                if (y < 0 || y > 100) {
                    errors.add("POSITION_Y_NOT_IN_MAP")
                    logger.info("POSITION_Y_NOT_IN_MAP")
                }

                if (errors.isNotEmpty()) {
                    ctx.status(400)
                    ctx.json(errors)
                    logger.info("ERROR_POSITION")
                    logger.info("END_POSITION")
                } else {
                    ctx.status(200)
                    ctx.json("SUCCESS_POSITION")
                }
            }

            //Guilde
            path("Guild") {
                get("allGuild") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("GET_ALL_GUILD")
                        ctx.json(Guild.allGuilds)
                        ctx.status(200)
                    }
                }

                get("members") { ctx ->
                    logger.info("GET_ALL_MEMBERS_GUILD")
                    ctx.status(501)
                }

                put("deleteMember") { ctx ->
                    ctx.status(501)
                }

                post("addMember") { ctx ->
                    ctx.status(501)
                }

                post("createGuild") { ctx ->
                    Certification.verification(ctx) {
                        logger.info("POST_GUILD")
                        var errors = arrayListOf<String>()
                        if (ctx.formParam("nameGuild").isNullOrEmpty()) {
                            errors.add("NAME_GUILD_REQUIRED")
                            logger.info("NAME_GUILD_REQUIRED")
                        }

                        if (errors.isNotEmpty()) {
                            ctx.status(400)
                            ctx.json(errors)
                            logger.info("END_CREATE_GUILD")
                        } else {
                            ctx.json("SUCCESS_CREATE_GUILD")
                            ctx.status(201)
                        }
                    }
                }
            }


            //MAP
            get("map") { ctx ->
                logger.info("RECUP_MAP")
                ctx.status(501)
            }
        }
    }
}