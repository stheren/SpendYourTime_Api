package com.spendyourtime.data

import com.google.common.hash.Hashing
import com.spendyourtime.helpers.Database
import java.nio.charset.StandardCharsets

class User(var email: String, var pseudo: String, passwordText: String, var player : Player = Player()){

    var password : String = Hashing.sha256()
        .hashString(passwordText, StandardCharsets.UTF_8)
        .toString();

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