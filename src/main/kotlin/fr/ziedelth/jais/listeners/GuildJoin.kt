/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.listeners

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.getJGuild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class GuildJoin(val commands: Array<Command>) : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        super.onGuildJoin(event)
        val guild = event.guild

        guild.getJGuild()
        val cl = this.commands.map { command ->
            CommandData(
                command.name,
                command.description
            ).addOptions(command.options.map { option ->
                OptionData(
                    option.type,
                    option.name,
                    option.description,
                    option.required
                )
            })
        }


        if (!Const.PUBLIC) guild.updateCommands().addCommands(cl).submit()
        else guild.updateCommands().submit()

        JLogger.info("I've join a new guild! (${guild.name}, ${guild.memberCount})")
    }
}