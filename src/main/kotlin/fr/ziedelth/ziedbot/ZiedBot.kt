package fr.ziedelth.ziedbot

import fr.ziedelth.ziedbot.listeners.GuildMessageReactionAdd
import fr.ziedelth.ziedbot.listeners.SlashCommand
import fr.ziedelth.ziedbot.threads.animes.AnimeEpisodesThread
import fr.ziedelth.ziedbot.threads.animes.AnimeNewsThread
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.getZiedGuild
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User

object ZiedBot {
    lateinit var master: User
    var image: String? = null
    val jda: JDA = JDABuilder.createDefault(Const.DISCORD_TOKEN.token).build()

    @JvmStatic
    fun main(args: Array<String>) {
        ZiedLogger.info("Init...")
        ZiedLogger.info("Request per day: ${(60L / Const.DELAY_BETWEEN_REQUEST) * 24L}")

        jda.awaitReady()
        image = jda.selfUser.avatarUrl
        jda.addEventListener(SlashCommand(), GuildMessageReactionAdd())
        ZiedLogger.info("Connected to ${jda.guilds.size} guild(s)!")
        jda.guilds.forEach { it.getZiedGuild() }
        jda.retrieveUserById(132903783792377856L).queue { user -> master = user }
        jda.presence.activity = Activity.playing("bugged with master")

        AnimeEpisodesThread()
        AnimeNewsThread()
    }
}