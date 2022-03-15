package com.spendyourtime.data

class User(var email: String, var pseudo: String, var password: String, var player : Player){

    companion object {
        var allUsers = arrayListOf<User>()
    } 

    init{
        if(allUsers.contains(this))
            throw Exception("Player already in allUsers")
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