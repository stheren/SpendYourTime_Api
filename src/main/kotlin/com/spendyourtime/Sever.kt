package com.spendyourtime

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.slf4j.LoggerFactory


object Server {
    class Position(var x: Int, var y: Int)
    class Skin(var body: Int, var accesories: Int, var hairstyle: Int, var eyes: Int, var outfit: Int)
    class User(var email: String, var pseudo: String, var password: String, var skin: Skin)
    class Player(var id: Int, var user: User, var position: Position)

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
                    logger.info("POST_REGISTER")
                    ctx.status(501)
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
            }

            //Player
                app.post("/Player/position") { ctx ->
                    logger.info("GET_PLAYER_POSITION")
                    ctx.status(501).json(ctx.status(501))
                }

                app.put("User/skin") { ctx ->
                    logger.info("CHANGE_SKIN_PLAYER")
                    ctx.status(501)
                }


            //MAP
            get("map") { ctx ->
                logger.info("RECUP_MAP")
                ctx.status(501)
            }
        }
    }
}