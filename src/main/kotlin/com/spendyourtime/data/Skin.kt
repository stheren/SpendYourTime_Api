package com.spendyourtime.data

class Skin(var body: Int, var accesories: Int, var hairstyle: Int, var eyes: Int, var outfit: Int){
    
    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Skin 
        && other.body == this.body 
        && other.accesories == this.accesories 
        && other.hairstyle == this.hairstyle
        && other.eyes == this.eyes
        && other.outfit == this.outfit
    }
}