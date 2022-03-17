package com.spendyourtime.data

import com.spendyourtime.helpers.Database
import com.spendyourtime.helpers.Sha512

class User(var email: String, var pseudo: String, passwordText: String, var player : Player = Player()){

    var password : String = Sha512.encode(passwordText)

    init{
        if(Database.allUsers.contains(this))
            throw Exception("Player already in allUsers")
        for(user in Database.allUsers){
            if (user.pseudo == this.pseudo)
                throw Exception("Pseudo already exists")
            if(user.email == this.email)
                throw Exception("Email already exists")
        }
        Database.allUsers.add(this)
    }

    

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
}