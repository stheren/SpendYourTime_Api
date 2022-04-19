package com.spendyourtime.data

import com.spendyourtime.helpers.Database

class Guild(var name: String, var owner: User, var typeWork: Work) {

    companion object {
        private fun getUniqueID(): Int {
            var key = 0
            while (Database.allGuilds.any { it.id == key }) {
                key++
            }
            return key
        }
    }

    // Create a autoincrement id
    var id: Int = getUniqueID()

    var place = Map()
        get() {
            return field.get(Database.allUsers.filter { it.player.currentGuildMap == this.id }.map { it.player })
        }

    var waitingList = arrayListOf<User>()
    var employees = arrayListOf<User>()
    var tasks = arrayListOf<Work>()

    init {
        for (g in Database.allGuilds) {
            if (g.name == name)
                throw Exception("This name is already used")
            if (g.owner.equals(owner))
                throw Exception("This owner alreagy have a guild")
        }
    }

    fun IsInGuild(u: User): Boolean {
        return employees.any { it == u } || owner == u
    }

    /**
     * Add task to tasks list
     */
    fun AddTask(w: Work) {
        tasks.add(w)
    }

    /**
     * Remove task from list
     * Return false if task is not in list
     */
    fun RemoveTask(w: Work): Boolean {
        if (tasks.contains(w)) {
            tasks.remove(w)
            return true
        }
        return false
    }


    /**
     * Add a member to a guild
     * Return false if it's the owner or if p is already in the guild
     */
    fun AddMember(u: User): Boolean {
        if (owner.equals(u) || employees.contains(u))
            return false
        waitingList.add(u)
        return true
    }

    fun AcceptMember(u: User) {
        if (waitingList.contains(u)) {
            employees.add(u)
            waitingList.remove(u)
        }
    }

    fun RemoveFromWaitingList(u: User) {
        if (waitingList.contains(u)) {
            waitingList.remove(u)
        }
    }

    /**
     * Remove a player from a guild
     * Return false if it's the owner or if p is not in the guild
     */
    fun RemoveMember(u: User): Boolean {
        if (u.equals(owner))
            return false
        if (employees.contains(u)) {
            employees.remove(u)
            return true
        }
        return false
    }


}