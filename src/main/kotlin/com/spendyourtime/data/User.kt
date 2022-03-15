package com.spendyourtime.data

class User(var email: String, var pseudo: String, var password: String, var player : Player = Player()){

    companion object {
        var allUsers = arrayListOf<User>()
    } 

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

    fun findUserByPseudo(pseudo :String) : User?{
        return allUsers.find { it.pseudo == pseudo }
    }
    fun findUserByEmail(email :String) : User?{
        return allUsers.find { it.email == email }
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