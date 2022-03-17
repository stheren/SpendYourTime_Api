package com.spendyourtime.data

enum class Work {
    Work1,
    Work2;

    companion object {
        fun validateWork(work: String): Boolean {
            return Work.values().any {
                it.name == work
            }
        }

        fun sendWork(work: String): Work {
            return Work.values().first {
                it.name == work
            }
        }
    }
}