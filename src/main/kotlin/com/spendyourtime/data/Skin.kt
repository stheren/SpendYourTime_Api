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

    constructor() : this(1,1,1,1,1)

    init {
        if (body in (maxBody + 1) until minBody)
            throw Exception("Body must be between $minBody and $maxBody")
        if (accessoiries in (maxAccessoiries + 1) until minAccessoiries)
            throw Exception("accessoiries must be between $minAccessoiries and $maxAccessoiries")
        if (hairstyle in (maxHairstyle + 1) until minHairstyle)
            throw Exception("hairstyle must be between $minHairstyle and $maxAccessoiries")
        if (eyes in (maxEyes + 1) until minEyes)
            throw Exception("eyes must be between $minEyes and $maxEyes")
        if (outfit in (maxOutfit + 1) until minOutfit)
            throw Exception("outfit must be between $minOutfit and $maxOutfit")
    }


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