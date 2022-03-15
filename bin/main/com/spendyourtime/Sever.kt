package com.spendyourtime

import io.javalin.Javalin
import org.slf4j.LoggerFactory

object Server {
    class Position(var x: Int, var y: Int, var name: String)
    class Skin(var body: Int, var accesories: Int, var hairstyle: Int, var eyes: Int, var outfit: Int)
    class User(var email: String, var pseudo: String, var password: String, var skin: Skin)

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
            app.post("/User/register") { ctx ->
                logger.info(ctx.body())
                ctx.status(501)
            }

            app.post("/user/login") { ctx ->
                logger.info(ctx.body())
                ctx.status(501)
            }

            app.post("/user/name") { ctx ->
                logger.info(ctx.body())
                ctx.status(501)
            }

            app.get("/user/skin") { ctx ->
                logger.info(ctx.body())
                ctx.status(501)
            }


            //Player

            app.post("/player/position") { ctx ->
                logger.info(ctx.body())
                ctx.status(501)
            }

            app.get("/player/skin") { ctx ->
                logger.info("RECUP_MAP")
                ctx.status(501)
            }

            app.put("/player/skin") { ctx ->
                logger.info("ACCESS_PLAYER_SKIN")
                ctx.status(501)
            }

            //MAP
            app.get("/map") { ctx->
                logger.info("RECUP_MAP")
                ctx.status(501)
            }
        }
    }
}