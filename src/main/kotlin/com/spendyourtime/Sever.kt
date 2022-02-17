package com.spendyourtime

import io.javalin.Javalin

object Server {
    @JvmStatic
    fun main(args: Array<String>) {

        val app = Javalin.create().apply {
            exception(Exception::class.java) { e, ctx -> ctx.json("Not found") }
            this.error(404) { ctx ->
                ctx.json("Error")
            }
        }.start(7000)

        app.routes {
            app.get("/ping") { ctx ->
                ctx.json("Pong")
            }
            //USER INPUT API

            app.get("/position") { ctx ->
                ctx
            }
        }
    }
}