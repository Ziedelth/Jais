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

class GuildJoin(val commands: Array<Command>) : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        super.onGuildJoin(event)
        val guild = event.guild

        guild.getJGuild()

        if (!Const.PUBLIC) {
            val clua = guild.updateCommands()

            this.commands.forEach { command ->
                run {
                    val commandData = CommandData(command.name, command.description)
                    command.options.forEach { option ->
                        commandData.addOption(
                            option.type,
                            option.name,
                            option.description,
                            option.required
                        )
                    }

                    clua.addCommands(commandData)
                }
            }

            clua.submit()
        } else guild.updateCommands().submit()

        JLogger.info("I've join a new guild! (${guild.name}, ${guild.memberCount})")
    }
}