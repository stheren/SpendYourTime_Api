package com.spendyourtime.data

import com.spendyourtime.helpers.DatabaseConnect
import com.spendyourtime.helpers.Jsonbase
import com.spendyourtime.helpers.Sha512

data class User(var email: String, var pseudo: String, var password: String, var player: Player = Player()){


    companion object {
        var id: Int = 0
//        private fun getUniqueID(): Int {
//            var key = 0
//            while (Jsonbase.allUsers.any { it.id == key }) {
//                key++
//            }
//            return key
//        }

        fun IncrementId() {
            User.id++
        }
    }


    init{
        password = Sha512.encode(password)
        if (DatabaseConnect.AllUsers.findUserByPseudo(this.pseudo) != null)
            throw Exception("PSEUDO_ALREADY_EXIST")
        if(DatabaseConnect.AllUsers.findUserByEmail(this.email) != null)
            throw Exception("EMAIL_ALREADY_EXIST")
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
//    fun toJSON(): String{
//        return "{\"id\":$id,\"email\":\"$email\",\"pseudo\":\"$pseudo\",\"player\":${player.toJSON()}}"
//    }
}