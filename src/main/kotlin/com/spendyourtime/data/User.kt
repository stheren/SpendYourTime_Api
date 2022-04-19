package com.spendyourtime.data

import com.spendyourtime.helpers.Database
import com.spendyourtime.helpers.Sha512

data class User(var email: String, var pseudo: String, var password: String, var player : Player = Player()){

    companion object {
        private fun getUniqueID(): Int {
            var key = 0
            while (Database.allUsers.any { it.id == key }) {
                key++
            }
            return key
        }
    }

    var id: Int = getUniqueID()
    init{
        password = Sha512.encode(password)
        if(Database.allUsers.contains(this))
            throw Exception("Player already in allUsers")
        for(user in Database.allUsers){
            if (user.pseudo == this.pseudo)
                throw Exception("Pseudo already exists")
            if(user.email == this.email)
                throw Exception("Email already exists")
        }
    }

    constructor() : this("","","") {}
    

    override fun equals(other: Any?) : Boolean{
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + pseudo.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + player.hashCode()
        return result
    }

    // Function to return data of the user without password on JSON format
    fun toJSON(): String{
        return "{\"id\":$id,\"email\":\"$email\",\"pseudo\":\"$pseudo\",\"player\":${player.toJSON()}}"
    }
}