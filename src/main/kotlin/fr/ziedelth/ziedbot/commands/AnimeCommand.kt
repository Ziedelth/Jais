package fr.ziedelth.ziedbot.commands

import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.commands.Option
import fr.ziedelth.ziedbot.utils.getZiedGuild
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class AnimeCommand : Command(
    "anime",
    "Register a channel to send anime news and episodes",
    arrayOf(Option("channel", "Select a text channel", OptionType.CHANNEL, true))
) {
    override fun execute(event: SlashCommandEvent) {
        event.deferReply().queue()
        val messageChannel: MessageChannel? = event.getOption("channel")?.asMessageChannel

        if (messageChannel != null && messageChannel.type == ChannelType.TEXT) {
            event.guild!!.getZiedGuild().animeChannel = messageChannel as TextChannel?
            event.hook.sendMessage("Successfully register!").setEphemeral(true).queue()
        } else event.hook.sendMessage("Error, please select a text channel").setEphemeral(true).queue()
    }
}