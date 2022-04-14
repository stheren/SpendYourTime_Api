package com.spendyourtime.data

class Message(val id: Int, val text: String, val user: User, val date: Long = System.currentTimeMillis())