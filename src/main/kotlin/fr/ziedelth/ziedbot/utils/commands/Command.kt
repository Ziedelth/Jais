package fr.ziedelth.ziedbot.utils.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

abstract class Command(
    val name: String,
    val description: String = "No description...",
    val options: Array<Option> = arrayOf(),
    val permission: Permission = Permission.ADMINISTRATOR
) {
    abstract fun execute(event: SlashCommandEvent)

    fun sendInPrivate(event: SlashCommandEvent, message: String) =
        event.acknowledge(true).queue { commandHook -> commandHook.sendMessage(message).queue() }
}