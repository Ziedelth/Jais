/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.commands.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SlashCommand(private val commands: Array<Command>) : ListenerAdapter() {
    override fun onSlashCommand(event: SlashCommandEvent) {
        super.onSlashCommand(event)
        if (event.guild == null) return

        val member: Member? = event.member
        val commandName: String = event.name
        val command: Command? =
            this.commands.firstOrNull { command -> command.name == commandName && member?.hasPermission(command.permission) == true }
        if (command != null) command.execute(event) else event.reply("I can't handle that command right now :(")
            .setEphemeral(true).queue()
    }
}