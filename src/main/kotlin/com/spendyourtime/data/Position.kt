package com.spendyourtime.data

class Position(var x: Int, var y: Int){

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Position 
        && other.x == this.x 
        && other.y == this.y
    }
}