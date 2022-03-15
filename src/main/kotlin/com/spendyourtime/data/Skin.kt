package com.spendyourtime.data

class Skin(var body: Int, var accessoiries: Int, var hairstyle: Int, var eyes: Int, var outfit: Int){
    
    companion object {
        val minBody = 1
        val maxBody = 9
        val minAccessoiries = 1
        val maxAccessoiries = 1
        val minHairstyle = 1
        val maxHairstyle = 1
        val minEyes = 1
        val maxEyes = 1
        val minOutfit = 1
        val maxOutfit = 1
    }

    init{
        if(body < minBody || maxBody < body)
            throw Exception("Body must be between " + minBody + " and " + maxBody)
        if(accessoiries < minAccessoiries || maxAccessoiries < accessoiries)
            throw Exception("accessoiries must be between " + minAccessoiries + " and " + maxAccessoiries)
        if(hairstyle < minHairstyle || maxHairstyle < hairstyle)
            throw Exception("hairstyle must be between " + minHairstyle + " and " + maxAccessoiries)
        if(eyes < minEyes || maxEyes < eyes)
            throw Exception("eyes must be between " + minEyes + " and " + maxEyes)
        if(outfit < minOutfit || maxOutfit < outfit)
            throw Exception("outfit must be between " + minOutfit + " and " + maxOutfit)
    }

    Skin() : Skin(1, 1, 1, 1)

    override fun equals(other: Any?) : Boolean{
        return other != null 
        && other is Skin 
        && other.body == this.body 
        && other.accessoiries == this.accessoiries 
        && other.hairstyle == this.hairstyle
        && other.eyes == this.eyes
        && other.outfit == this.outfit
    }
}