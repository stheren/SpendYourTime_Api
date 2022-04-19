package com.spendyourtime.data

class Skin(var body: Int, var accessories: Int, var hairstyle: Int, var eyes: Int, var outfit: Int){
    
    companion object {
        const val minBody = 1
        const val maxBody = 9
        const val minAccessories = 1
        const val maxAccessories = 2
        const val minHairstyle = 1
        const val maxHairstyle = 2
        const val minEyes = 1
        const val maxEyes = 2
        const val minOutfit = 1
        const val maxOutfit = 2
    }

    constructor() : this(1,1,1,1,1)

    init{
        if(body !in minBody until maxBody)
            throw Exception("INVALID_BODY")
        if(accessories !in minAccessories until maxAccessories)
            throw Exception("INVALID_ACCESSORIES")
        if(hairstyle !in minHairstyle until maxHairstyle)
            throw Exception("INVALID_HAIRSTYLE")
        if(eyes !in minEyes until maxEyes)
            throw Exception("INVALID_EYES")
        if(outfit !in minOutfit until maxOutfit)
            throw Exception("INVALID_OUTFIT")
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

    fun toJSON() : String{
        return "{\"body\":$body,\"accessories\":$accessories,\"hairstyle\":$hairstyle,\"eyes\":$eyes,\"outfit\":$outfit}"
    }
}