package com.spendyourtime.helpers

import io.javalin.http.Context
import org.slf4j.LoggerFactory


fun Context.retour(code : Int, msg : String) {
    LoggerFactory.getLogger(this::class.java).info(msg)
    this.json(msg).status(code)
}

fun Context.retour(code : Int, obj : Any) {
    this.status(code)
    this.json(obj)
}