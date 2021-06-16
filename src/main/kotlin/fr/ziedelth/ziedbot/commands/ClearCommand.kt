package fr.ziedelth.ziedbot.commands

import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.commands.Option
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.CompletableFuture

class ClearCommand : Command(
    "clear",
    "Delete x lasted messages in a channel. By default, x: 50",
    mutableListOf(
        Option(
            "x",
            "Number of messages need to be delete",
            OptionType.INTEGER
        )
    ).toTypedArray()
) {

    override fun execute(event: SlashCommandEvent) {
        val count = (event.getOption("x")?.asLong ?: 50).toInt()

        if (count !in 1..100) {
            event.reply("Select x in range between 1 and 100...").setEphemeral(true).queue()
            return
        }

        val channel: MessageChannel = event.channel

        event.reply("Clearing $count messages...").setEphemeral(true).queue { commandHook ->
            channel.history.retrievePast(count).queue {
                CompletableFuture.allOf(*channel.purgeMessages(it).toTypedArray()).whenComplete { _, _ ->
                    run {
                        commandHook.editOriginal("Finished!").queue()
                    }
                }
            }
        }
    }
}