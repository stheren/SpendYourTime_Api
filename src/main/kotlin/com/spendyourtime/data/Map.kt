package com.spendyourtime.data

class Map {
    var background : MutableList<MutableList<Int>> = MutableList(10) { MutableList(10) { 0 } }
    var foreground : MutableList<MutableList<Int>> = MutableList(10) { MutableList(10) { 0 } }
    var objects : MutableList<MutableList<Int>> = MutableList(10) { MutableList(10) { 0 } }

    var players  : MutableList<Player> = arrayListOf()

    init {
        for (i in 0..9) {
            for (j in 0..9) {
                background[i][j] = (Math.random() * 2).toInt()
                foreground[i][j] = (Math.random() * 2).toInt()
                objects[i][j] = (Math.random() * 2).toInt()
            }
        }
    }

    fun get(l : List<Player>) : Map {
        players.clear()
        players.addAll(l)
        return this
    }


}