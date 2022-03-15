package com.spendyourtime

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
                    if(ctx.formParam("email").isNullOrEmpty()){
                        errors.add("EMAIL_IS_EMPTY")
                        logger.info("EMAIL_IS_EMPTY")
                    }
                    if(ctx.formParam("pseudo").isNullOrEmpty()){
                        errors.add("PSEUDO_IS_EMPTY")
                        logger.info("PSEUDO_IS_EMPTY")
                    }
                    if(ctx.formParam("password").isNullOrEmpty()){
                        errors.add("PASSWORD_IS_EMPTY")
                        logger.info("PASSWORD_IS_EMPTY")
                    }


                    if(errors.isNotEmpty()){
                        ctx.status(400).json(errors)
                        logger.info("FAILED_OF_REGISTER")
                        logger.info("END_OF_REGISTER")
                    }
                    else{
                        //var u: User = User(ctx.formParam("email").toString(), ctx.formParam("pseudo").toString(), ctx.formParam("password").toString(), Skin())
                        ctx.json("Success register")
                        ctx.status(201)
                        logger.info("SUCCESS_REGISTER")
                        logger.info("END_OF_REGISTER")
                    }
                }

                post("login") { ctx ->
                    logger.info("POST_LOGIN")
                    ctx.status(501)
                }

                post("name") { ctx ->
                    logger.info("POST_NAME")
                    ctx.status(501)
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
                    logger.info("POST_PLAYER_POSITION")
                    ctx.status(501).json(ctx.status(501))
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