package com.spendyourtime.data

class User(var email: String, var pseudo: String, var password: String, var skin: Skin, var player : Player){

    companion object {
        var allUsers = arrayListOf<User>()
    } 

    init{
        if(allUsers.contains(this))
            throw Exception("Player already in allUsers")
        allUsers.add(this)
    }

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is User 
        && other.email.equals(this.email) 
        && other.pseudo.equals(this.pseudo) 
        && other.password.equals(this.password) 
        && other.skin.equals(this.skin) 
    }
}