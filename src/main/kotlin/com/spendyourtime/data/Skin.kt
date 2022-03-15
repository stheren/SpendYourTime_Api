package com.spendyourtime.data

class Skin(var body: Int, var accessories: Int, var hairstyle: Int, var eyes: Int, var outfit: Int){
    
    companion object {
        const val minBody = 1
        const val maxBody = 9
        const val minAccessories = 1
        const val maxAccessories = 1
        const val minHairstyle = 1
        const val maxHairstyle = 1
        const val minEyes = 1
        const val maxEyes = 1
        const val minOutfit = 1
        const val maxOutfit = 1
    }

    constructor() : this(1,1,1,1,1)

    init{
        if(body !in minBody until maxBody)
            throw Exception("Body must be between $minBody and $maxBody")
        if(accessories !in minAccessories until maxAccessories)
            throw Exception("accessoiries must be between $minAccessories and $maxAccessories")
        if(hairstyle in minHairstyle until maxHairstyle)
            throw Exception("hairstyle must be between $minHairstyle and $maxAccessories")
        if(eyes !in maxEyes until minEyes)
            throw Exception("eyes must be between $minEyes and $maxEyes")
        if(outfit !in minOutfit until maxOutfit)
            throw Exception("outfit must be between $minOutfit and $maxOutfit")
    }


    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Skin 
        && other.body == this.body 
        && other.accessories == this.accessories
        && other.hairstyle == this.hairstyle
        && other.eyes == this.eyes
        && other.outfit == this.outfit
    }
}