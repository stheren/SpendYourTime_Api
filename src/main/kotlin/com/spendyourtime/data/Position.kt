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
            throw Exception("x must be between $minX and $maxX")
        if(y !in minY until maxY)
            throw Exception("y must be between $minY and $maxY")
    }

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Position 
        && other.x == this.x 
        && other.y == this.y
    }
}