package fr.ziedelth.ziedbot

import fr.ziedelth.ziedbot.listeners.GuildMessageReactionAdd
import fr.ziedelth.ziedbot.listeners.SlashCommand
import fr.ziedelth.ziedbot.threads.AnimeThread
import fr.ziedelth.ziedbot.threads.CommandsThread
import fr.ziedelth.ziedbot.threads.NewsThread
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.temporal.TemporalAccessor

object ZiedBot {
    private lateinit var master: User
    val jda: JDA = JDABuilder.createDefault(Const.discordToken.token).build()

    @JvmStatic
    fun main(args: Array<String>) {
        jda.addEventListener(SlashCommand(this), GuildMessageReactionAdd())
        jda.awaitReady()
        ZiedLogger.info("Connected to ${jda.guilds.size} guild(s)!")
        jda.retrieveUserById(132903783792377856L).queue { user -> master = user }
        jda.presence.activity = Activity.playing("bugged with master")

        CommandsThread()
        AnimeThread(this)
        NewsThread(this)
    }

    fun setEmbed(
        author: String?,
        authorUrl: String?,
        authorIcon: String?,
        title: String?,
        titleUrl: String?,
        thumbnail: String?,
        description: String?,
        color: Color?,
        image: String?,
        timestamp: TemporalAccessor?
    ): EmbedBuilder {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setAuthor(author, authorUrl, authorIcon)
        embedBuilder.setTitle(title, titleUrl)
        embedBuilder.setThumbnail(if (thumbnail == null || thumbnail.isEmpty()) master.avatarUrl else thumbnail)
        embedBuilder.setDescription(description)
        embedBuilder.setColor(color)
        embedBuilder.setImage(image)
        embedBuilder.setFooter("Powered by Ziedelth.fr \uD83D\uDDA4", "https://ziedelth.fr/images/brand.jpg")
        embedBuilder.setTimestamp(timestamp)
        return embedBuilder
    }
}