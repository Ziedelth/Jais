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
        Option("channel", "Select a text channel", OptionType.CHANNEL, true),
        Option("language", "Select a language with the flag, by default: French", OptionType.STRING),
        Option("unregister", "Unregister the channel, by default: false", OptionType.BOOLEAN)
    )
) {
    override fun execute(event: SlashCommandEvent) {
        val ziedGuild = event.guild!!.getZiedGuild()
        val unregister = event.getOption("unregister")?.asBoolean ?: false

        if (!unregister) {
            val messageChannel = event.getOption("channel")?.asMessageChannel!!

            if (messageChannel.type != ChannelType.TEXT) {
                event.reply("Error, please select a text channel").setEphemeral(true).queue()
                return
            }

            val sLanguage = event.getOption("language")?.asString ?: Language.FRENCH.flag

            if (!Language.values().any { it.flag.equals(sLanguage, true) }) {
                event.reply("Error, please choose a correct country").setEphemeral(true).queue()
                return
            }

            val language = Language.values().find { it.flag.equals(sLanguage, true) }!!

            ziedGuild.animeChannels.put(messageChannel.id, language).run { ziedGuild.save() }
            event.reply("Successfully register!").setEphemeral(true).queue()
        } else {
            val messageChannel = event.getOption("channel")?.asMessageChannel!!

            if (messageChannel.type != ChannelType.TEXT) {
                event.reply("Error, please select a text channel").setEphemeral(true).queue()
                return
            }

            ziedGuild.animeChannels.remove(messageChannel.id).run { ziedGuild.save() }
            event.reply("Successfully unregister!").setEphemeral(true).queue()
        }
    }
}