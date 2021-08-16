/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.commands

import fr.ziedelth.jais.utils.commands.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import java.util.concurrent.CompletableFuture

class ClearCommand : Command(
    "clear",
    "Clear a channel. Only for administrators",
) {
    override fun execute(event: SlashCommandEvent) {
        val textChannel = event.textChannel

        if (event.member!!.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("Editing...").setEphemeral(true).queue { message ->
                textChannel.history.retrievePast(100).queue {
                    CompletableFuture.allOf(*textChannel.purgeMessages(it).toTypedArray()).whenComplete { _, _ ->
                        run {
                            message.editOriginal("Finished!").queue()
                        }
                    }
                }
            }
        } else event.reply("You've not permission to use this command!").setEphemeral(true).queue()
    }
}