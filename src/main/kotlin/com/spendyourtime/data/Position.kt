package com.spendyourtime.data

class Position(var x: Int, var y: Int){

    companion object {
        val minX = Int.MIN_VALUE
        val maxX = Int.MAX_VALUE
        val minY = Int.MIN_VALUE
        val maxY = Int.MAX_VALUE
    }

    init {
        if(x !in minX until maxX)
            throw Exception("INVALID_X")
        if(y !in minY until maxY)
            throw Exception("INVALID_Y")
    }

    constructor() : this(0,0) {}

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Position 
        && other.x == this.x 
        && other.y == this.y
    }

    // toJSON
    fun toJSON() : String{
        return "{\"x\":$x,\"y\":$y}"
    }
}