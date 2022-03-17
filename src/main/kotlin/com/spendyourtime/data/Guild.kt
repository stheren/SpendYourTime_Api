package com.spendyourtime.data

class Guild(var name: String, var owner: Player, var typeWork : Work) {

    companion object {
        var allGuilds = arrayListOf<Guild>()
    }

    var employees = arrayListOf<Player>()
    var tasks = arrayListOf<Work>()

    init {
        for (g in allGuilds) {
            if (g.name == name)
                throw Exception("This name is already used")
            if (g.owner.equals(owner))
                throw Exception("This owner alreagy have a guild")
        }
        allGuilds.add(this);

    }

    fun IsInGuild(p : Player): Boolean{
       return employees.any{it == p} || owner == p
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
    fun AddMember(p: Player): Boolean {
        if (owner.equals(p) || employees.contains(p))
            return false
        employees.add(p)
        return true
    }

    /**
     * Remove a player from a guild
     * Return false if it's the owner or if p is not in the guild
     */
    fun RemoveMember(p: Player): Boolean {
        if (p.equals(owner))
            return false
        if (employees.contains(p)) {
            employees.remove(p)
            return true
        }
        return false
    }
}