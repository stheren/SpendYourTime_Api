package com.spendyourtime.data

import com.spendyourtime.helpers.Database

class Message(val text: String, val user: User, val date: Long = System.currentTimeMillis()){
    companion object {
        private fun getUniqueID(): Int {
            var key = 0
            while (Database.allChat.any { it.id == key }) {
                key++
            }
            return key
        }
    }

    var id: Int = getUniqueID()
}