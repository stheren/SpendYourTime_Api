package com.spendyourtime.data

class Position(var x: Int, var y: Int){

    companion object {
        val minX = Int.MIN_VALUE
        val maxX = Int.MAX_VALUE
        val minY = Int.MIN_VALUE
        val maxY = Int.MAX_VALUE
    }

    init {
        
    }

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Position 
        && other.x == this.x 
        && other.y == this.y
    }
}