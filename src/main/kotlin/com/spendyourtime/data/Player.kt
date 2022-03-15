package com.spendyourtime.data


class Player(var id: Int, var position: Position, var skin: Skin){

    override fun equals(other: Any?) : Boolean{
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + position.hashCode()
        result = 31 * result + skin.hashCode()
        return result
    }

}

