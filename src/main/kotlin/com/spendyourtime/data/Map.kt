package com.spendyourtime.data

class Map {
    var background : MutableList<MutableList<Int>> = MutableList(10) { MutableList(10) { 0 } }
    var foreground : MutableList<MutableList<Int>> = MutableList(10) { MutableList(10) { 0 } }
    var objects : MutableList<MutableList<Int>> = MutableList(10) { MutableList(10) { 0 } }

    init {
        for (i in 0..9) {
            for (j in 0..9) {
                background[i][j] = (Math.random() * 2).toInt()
                foreground[i][j] = (Math.random() * 2).toInt()
                objects[i][j] = (Math.random() * 2).toInt()
            }
        }
    }




}