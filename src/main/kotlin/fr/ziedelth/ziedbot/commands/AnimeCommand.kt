package fr.ziedelth.ziedbot.commands

import fr.ziedelth.ziedbot.utils.animes.Country
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
        Option("country", "Select a country with the flag, by default: France", OptionType.STRING, true),
        Option("unregister", "Unregister the channel, by default: false", OptionType.BOOLEAN)
    )
) {
    override fun execute(event: SlashCommandEvent) {
        val ziedGuild = event.guild!!.getZiedGuild()
        val messageChannel = event.getOption("channel")?.asMessageChannel!!

        if (messageChannel.type != ChannelType.TEXT) {
            event.reply("Error, please select a text channel").setEphemeral(true).queue()
            return
        }

        val sCountry = event.getOption("country")?.asString ?: Country.FRANCE.flag
        val unregister = event.getOption("unregister")?.asBoolean ?: false

        if (!Country.values().any { it.flag.equals(sCountry, true) }) {
            event.reply("Error, please choose a correct country").setEphemeral(true).queue()
            return
        }

        val country = Country.values().find { it.flag.equals(sCountry, true) }!!
        val countries = ziedGuild.animeChannels[messageChannel.id] ?: mutableListOf()

        if (!unregister) {
            if (!countries.contains(country)) {
                countries.add(country)
                ziedGuild.animeChannels.put(messageChannel.id, countries).run { ziedGuild.save() }
                event.reply("Successfully register!").setEphemeral(true).queue()
            } else {
                event.reply("This channel contains already this country!").setEphemeral(true).queue()
            }
        } else {
            if (!countries.contains(country)) {
                event.reply("This channel not contains this country!").setEphemeral(true).queue()
            } else {
                countries.remove(country)
                ziedGuild.animeChannels.put(messageChannel.id, countries).run { ziedGuild.save() }
                event.reply("Successfully unregister!").setEphemeral(true).queue()
            }
        }
    }
}