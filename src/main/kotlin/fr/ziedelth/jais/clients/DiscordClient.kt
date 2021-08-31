/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.clients

import fr.ziedelth.jais.commands.ClearCommand
import fr.ziedelth.jais.commands.ConfigCommand
import fr.ziedelth.jais.commands.PlatformsCommand
import fr.ziedelth.jais.listeners.*
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.Const.toHHMMSS
import fr.ziedelth.jais.utils.Emoji.CLAP
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.tokens.DiscordToken
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.awt.Color
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.logging.Level

class DiscordClient : Client {
    private var jda: JDA? = null
    private var master: User? = null
    private var image: String? = null
    private val commands: Array<Command> = arrayOf(PlatformsCommand(), ClearCommand(), ConfigCommand())

    init {
        val token = Const.getToken("discord.json", DiscordToken::class.java) as DiscordToken?

        if (token != null) {
            try {
                this.jda = JDABuilder.createDefault(token.token).build()
                this.jda!!.presence.setPresence(OnlineStatus.IDLE, true)
                this.jda!!.awaitReady()
                val cl = this.commands.map { command ->
                    CommandData(
                        command.name,
                        command.description
                    ).addOptions(command.options.map { option ->
                        OptionData(
                            option.type,
                            option.name,
                            option.description,
                            option.required
                        )
                    })
                }

                this.jda!!.guilds.forEach { guild ->
                    guild.getJGuild()

                    if (!Const.PUBLIC) guild.updateCommands().addCommands(cl).submit()
                    else guild.updateCommands().submit()
                }

                if (Const.PUBLIC) {
                    if (!Const.PUBLIC) this.jda!!.updateCommands().addCommands(cl).submit()
                    else this.jda!!.updateCommands().submit()
                }

                this.jda!!.addEventListener(
                    SlashCommand(this.commands),
                    GuildMessageReceived(),
                    GuildMessageReactionAdd(),
                    GuildMessageReactionRemove(),
                    GuildJoin(this.commands),
                    GuildLeave()
                )
                this.update()
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Can not load ${this.javaClass.simpleName} client", exception)
            }
        }
    }

    override fun update() {
        JLogger.info("[${this.javaClass.simpleName}] Connected to ${this.jda!!.guilds.size} guild(s)!")
        this.image = this.jda!!.selfUser.avatarUrl
        this.jda!!.retrieveUserById(132903783792377856L).queue { user -> master = user }
        this.jda!!.presence.activity = Activity.playing("bugged with master")
        this.jda!!.presence.setPresence(OnlineStatus.ONLINE, false)
    }

    override fun sendEpisodes(episodes: Array<Episode>) {
        episodes.map { it.platform }.distinct().forEach { platform ->
            val l = episodes.filter { it.platform == platform }

            l.map { it.country }.distinct().forEach { country ->
                val cl = l.filter { it.country == country }

                // SPAM
                if (cl.size >= 10) {
                    val animes: Array<String> = cl.map { it.anime }.distinct().toTypedArray()
                    val stringBuilder = animes.map { "• $it\n" }.distinct().toString()

                    val embed = setEmbed(
                        author = platform.getName(),
                        authorUrl = platform.getURL(),
                        authorIcon = platform.getImage(),
                        title = "${cl.size} ${country.episode}s",
                        description = stringBuilder,
                        color = platform.getColor(),
                        image = cl.first().image
                    ).build()

                    sendAnimeMessage(country, embed)
                } else {
                    cl.forEach { episode ->
                        val embed = getEpisodeEmbed(episode).build()
                        sendAnimeMessage(country, embed)
                    }
                }
            }
        }
    }

    override fun sendNews(news: Array<News>) {
        news.forEach {
            val embed = getNewsEmbed(it).build()
            sendAnimeMessage(it.country, embed)
        }
    }

    private fun setEmbed(
        author: String? = null,
        authorUrl: String? = null,
        authorIcon: String? = null,
        title: String? = null,
        titleUrl: String? = null,
        thumbnail: String? = null,
        description: String? = null,
        color: Color? = Const.MAIN_COLOR,
        image: String? = null,
        footer: String? = null,
        timestamp: TemporalAccessor? = Calendar.getInstance().toInstant()
    ): EmbedBuilder {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setAuthor(author, authorUrl, authorIcon)
        embedBuilder.setTitle(title, titleUrl)
        embedBuilder.setThumbnail(if (thumbnail == null || thumbnail.isEmpty()) master?.avatarUrl else thumbnail)
        embedBuilder.setDescription(description)
        embedBuilder.setColor(color)
        if (!image.isNullOrEmpty()) embedBuilder.setImage(image)
        embedBuilder.setFooter(
            "${if (!footer.isNullOrEmpty()) "$footer  •  " else ""}Powered by Ziedelth.fr \uD83D\uDDA4",
            this.image
        )
        embedBuilder.setTimestamp(timestamp)
        return embedBuilder
    }

    private fun getEpisodeEmbed(episode: Episode): EmbedBuilder {
        return setEmbed(
            episode.platform.getName(),
            episode.platform.getURL(),
            episode.platform.getImage(),
            episode.anime,
            episode.url,
            description = """
                ${if (episode.title != null) "** ${episode.title} **" else ""}
                ${episode.country.season} ${episode.season} • ${episode.country.episode} ${episode.number}
                ${if (episode.duration > 0) "$CLAP ${episode.duration.toHHMMSS()}" else ""}
            """.trimIndent(),
            color = episode.platform.getColor(),
            image = episode.image,
            footer = if (episode.type == EpisodeType.SUBTITLED) episode.country.subtitled else episode.country.dubbed,
            timestamp = ISO8601.toCalendar(episode.calendar).toInstant()
        )
    }

    private fun getNewsEmbed(news: News): EmbedBuilder {
        return setEmbed(
            news.p?.getName(),
            news.p?.getURL(),
            news.p?.getImage(),
            news.title,
            news.link,
            description = """
            ** ${Const.substring(news.description, 450)}${if (news.description.length < 450) "" else "..."} **
            
            ${Const.substring(news.content, 1500)}${if (news.content.length < 1500) "" else "..."}
            """.trimIndent(),
            color = news.p?.getColor(),
            timestamp = ISO8601.toCalendar(news.calendar).toInstant()
        )
    }
}