/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

interface ClickRunnable {
    fun run(clickType: ClickType, user: Long)
}

enum class ClickType {
    UNKNOWN,
    ADD,
    REMOVE,
    ;
}