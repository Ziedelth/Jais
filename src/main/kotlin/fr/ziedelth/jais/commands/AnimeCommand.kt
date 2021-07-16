package fr.ziedelth.jais.commands

import fr.ziedelth.jais.utils.Channel
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.commands.Option
import fr.ziedelth.jais.utils.getJGuild
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class AnimeCommand : Command(
    "anime",
    "Register a channel to send anime news and episodes",
    arrayOf(
        Option("channel", "Select a text channel", OptionType.CHANNEL, true),
        Option("country", "Select a country with the flag, by default: France", OptionType.STRING, true),
        Option("anime", "Send animes, bu default: registered value", OptionType.BOOLEAN),
        Option("news", "Send news, bu default: registered value", OptionType.BOOLEAN),
        Option("unregister", "Unregister the channel, by default: false", OptionType.BOOLEAN)
    )
) {
    override fun execute(event: SlashCommandEvent) {
        val jGuild = event.guild!!.getJGuild()
        val messageChannel = event.getOption("channel")?.asMessageChannel!!
        val sCountry = event.getOption("country")?.asString ?: Country.FRANCE.flag
        val unregister = event.getOption("unregister")?.asBoolean ?: false
        val country = Country.values().find { it.flag.equals(sCountry, true) }!!
        val channel = jGuild.animeChannels[messageChannel.id] ?: Channel()

        if (messageChannel.type != ChannelType.TEXT) {
            event.reply("Error, please select a text channel").setEphemeral(true).queue()
            return
        }

        if (!Country.values().any { it.flag.equals(sCountry, true) }) {
            event.reply("Error, please choose a correct country").setEphemeral(true).queue()
            return
        }

        if (!unregister) {
            channel.anime = event.getOption("anime")?.asBoolean ?: channel.anime
            channel.news = event.getOption("news")?.asBoolean ?: channel.news
            if (!channel.countries.contains(country)) channel.countries.add(country)
            jGuild.animeChannels[messageChannel.id] = channel
            jGuild.save()

            event.reply("Successfully register!").setEphemeral(true).queue()
        } else {
            if (channel.countries.contains(country)) channel.countries.remove(country)
            jGuild.animeChannels[messageChannel.id] = channel
            jGuild.save()

            event.reply("Successfully unregister!").setEphemeral(true).queue()
        }
    }
}