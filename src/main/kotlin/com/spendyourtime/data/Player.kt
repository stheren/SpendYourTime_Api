package com.spendyourtime.data


class Player(var id: Int, var user: User, var position: Position){

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Player 
        && other.id == this.id && other.user.equals(this.user) && other.position.equals(this.position)
    }
    
}

