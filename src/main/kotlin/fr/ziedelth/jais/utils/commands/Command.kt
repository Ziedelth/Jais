/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

abstract class Command(
    val name: String,
    val description: String = "No description...",
    val options: Array<Option> = arrayOf(),
    val permission: Permission = Permission.ADMINISTRATOR
) {
    abstract fun execute(event: SlashCommandEvent)
}