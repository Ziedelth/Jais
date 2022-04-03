/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.commands

interface Command {
    fun onCommand(args: List<String>)
}