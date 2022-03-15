package com.spendyourtime.data

class User(var email: String, var pseudo: String, var password: String, var skin: Skin){

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is User 
        && other.email.equals(this.email) 
        && other.pseudo.equals(this.pseudo) 
        && other.password.equals(this.password) 
        && other.skin.equals(this.skin) 
    }
}