package com.spendyourtime.data

import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets

class User(var email: String, var pseudo: String, passwordText: String, var player : Player = Player()){

    companion object {
        var allUsers = arrayListOf<User>()

        fun findUserByPseudo(pseudo :String) : User?{
            return allUsers.find { it.pseudo == pseudo }
        }
        fun findUserByEmail(email :String) : User?{
            return allUsers.find { it.email == email }
        }
        fun checkPassword(pseudo : String, pswText : String) : Boolean{
            var u = findUserByPseudo(pseudo)
            if(u == null)
                throw Exception("Unknwon user")
            return u.password.equals(Hashing.sha256().hashString(pswText, StandardCharsets.UTF_8).toString())
        }
    }

    var password : String = Hashing.sha256()
        .hashString(passwordText, StandardCharsets.UTF_8)
        .toString();

    init{
        if(allUsers.contains(this))
            throw Exception("Player already in allUsers")
        for(user in allUsers){
            if (user.pseudo == this.pseudo)
                throw Exception("Pseudo already exists")
            if(user.email == this.email)
                throw Exception("Email already exists")
        }
        allUsers.add(this)
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