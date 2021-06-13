package fr.ziedelth.ziedbot.commands

import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.commands.Option
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.Button

class HelloCommand : Command(
    "hello",
    options = arrayOf(Option(name = "channel", type = OptionType.CHANNEL, required = true))
) {
    override fun execute(event: SlashCommandEvent) {
        event.deferReply().queue()
        val messageChannel: MessageChannel? = event.getOption("channel")?.asMessageChannel
        event.hook.sendMessage("ok").queue()

        messageChannel?.sendMessage("test buttons on simple message")?.setActionRow(Button.success("test", "TEST"))
            ?.queue()
    }
}