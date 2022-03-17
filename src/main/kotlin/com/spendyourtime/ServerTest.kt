package com.spendyourtime

import com.spendyourtime.data.User
import com.spendyourtime.helpers.Certification

object ServerTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val e = Certification.create(User("er", "er", "er"))

        println(e)

        Certification.find(e+"z"){

        }

        Certification.find(e){

        }
    }
}
