package com.spendyourtime

import com.spendyourtime.data.User
import com.spendyourtime.helpers.Certification

object ServerTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val e = Certification.create(User("Silvain", "Test", "ok"))

        Certification.find(e){
            println(it.email)
        }
    }
}