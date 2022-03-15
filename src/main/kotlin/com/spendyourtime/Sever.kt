package com.spendyourtime

import com.spendyourtime.data.Player
import com.spendyourtime.data.Skin
import com.spendyourtime.data.User
import com.spendyourtime.helpers.Certification
import com.spendyourtime.helpers.EmailValidator
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.slf4j.LoggerFactory


object Server {

    @JvmStatic
    fun main(args: Array<String>) {
        val logger = LoggerFactory.getLogger(this::class.java)

        val app = Javalin.create().apply {
            exception(Exception::class.java) { e, ctx -> ctx.json("Not found") }
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
                    if (EmailValidator.isEmailValid(ctx.formParam("email").toString())) {
                        errors.add("EMAIL_NOT_VALID")
                        logger.info("EMAIL_NOT_VALID")
                    }
                    if (User.findUserByEmail(ctx.formParam("email").toString()) != null) {
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
                    if (User.findUserByPseudo(ctx.formParam("pseudo").toString()) != null) {
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

                    if (ctx.formParam("pseudo").isNullOrEmpty()) {
                        errors.add("PSEUDO_IS_EMPTY")
                        logger.info("PSEUDO_IS_EMPTY")
                    }
                    if (ctx.formParam("password").isNullOrEmpty()) {
                        errors.add("PASSWORD_IS_EMPTY")
                        logger.info("PASSWORD_IS_EMPTY")
                    }

                    if (errors.isNotEmpty()) {
                        ctx.status(400).json(errors)
                        logger.info("FAILED_LOGIN_IN")
                        logger.info("END_OF_LOGIN")
                    } else {

                        //ctx.json(Certification.create(u))
                        ctx.json("Success_Login_in")
                        ctx.status(202)
                    }
                }

                get("skin") { ctx ->

                    logger.info("GET_SKIN")
                    ctx.status(501)
                }

                app.put("skin") { ctx ->
                    logger.info("CHANGE_SKIN_PLAYER")
                    ctx.status(501)
                }
            }

            //Player
            app.post("/Player/position") { ctx ->
                var errors = arrayListOf<String>()
                logger.info("PlayerPosition")
                if (ctx.formParam("posX").isNullOrEmpty()) {
                    errors.add("POSITION_X_ERROR")
                    logger.info("POSITION_X_ERROR")
                }
                if (ctx.formParam("posX").toInt() < 0 or ctx.formParam("posX").toInt() > 100) {
                    errors.add("POSITION_X_NOT_IN_MAP")
                    logger.info("POSITION_X_NOT_IN_MAP")
                }
                if (ctx.formParam("posY").isNullOrEmpty()) {
                    errors.add("POSITION_Y_ERROR")
                    logger.info("POSITION_Y_ERROR")
                }
                if (ctx.formParam("posY").toInt() < 0 or ctx.formParam("posY").toInt() > 100) {
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
                    logger.info("GET_ALL_GUILD")
                    ctx.status(501)
                }

                get("members") { ctx ->
                    logger.info("GET_ALL_MEMBERS_GUILD")
                    ctx.status(501)
                }

                post("createGuild") { ctx ->
                    logger.info("POST_GUILD")
                    ctx.status(501)
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