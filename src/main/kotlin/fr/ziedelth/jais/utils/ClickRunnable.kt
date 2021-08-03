package fr.ziedelth.jais.utils

interface ClickRunnable {
    fun run(reaction: Reaction, clickType: ClickType, user: Long)
}

enum class ClickType {
    UNKNOWN,
    ADD,
    REMOVE,
    ;
}