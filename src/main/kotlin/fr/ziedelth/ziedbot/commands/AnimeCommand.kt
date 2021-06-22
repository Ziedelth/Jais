package fr.ziedelth.ziedbot.commands

import fr.ziedelth.ziedbot.utils.animes.Language
import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.commands.Option
import fr.ziedelth.ziedbot.utils.getZiedGuild
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class AnimeCommand : Command(
    "anime",
    "Register a channel to send anime news and episodes",
    arrayOf(
        Option("language", "Select a language with the flag", OptionType.STRING, true),
        Option("channel", "Select a text channel", OptionType.CHANNEL, true)
    )
) {
    override fun execute(event: SlashCommandEvent) {
        val sLanguage = event.getOption("language")?.asString!!

        if (!Language.values().any { it.flag.equals(sLanguage, true) }) {
            event.reply("Error, please choose a correct country").setEphemeral(true).queue()
            return
        }

        val language = Language.values().find { it.flag.equals(sLanguage, true) }!!
        val messageChannel = event.getOption("channel")?.asMessageChannel!!

        if (messageChannel.type != ChannelType.TEXT) {
            event.reply("Error, please select a text channel").setEphemeral(true).queue()
            return
        }

        event.guild!!.getZiedGuild().animeChannels.put(language, messageChannel.id)
            .run { event.guild!!.getZiedGuild().save() }
        event.reply("Successfully register!").setEphemeral(true).queue()
    }
}