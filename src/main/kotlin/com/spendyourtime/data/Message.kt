package com.spendyourtime.data

import com.spendyourtime.helpers.Jsonbase

class Message(val text: String, val user: User?, val date: Long = System.currentTimeMillis()){
    companion object {
        private fun getUniqueID(): Int {
            var key = 0
            while (Jsonbase.allChat.any { it.id == key }) {
                key++
            }
            return key
        }
    }

    var id: Int = getUniqueID()

    constructor() : this("", null) {}
}