package com.spendyourtime.data


class Player(var position: Position, var skin: Skin){
    constructor() : this(Position(0,0), Skin())

    override fun equals(other: Any?) : Boolean{
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        var result = 31 + position.hashCode()
        result = 31 * result + skin.hashCode()
        return result
    }

}

