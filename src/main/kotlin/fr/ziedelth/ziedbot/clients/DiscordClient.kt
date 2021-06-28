package fr.ziedelth.ziedbot.clients

import fr.ziedelth.ziedbot.commands.AnimeCommand
import fr.ziedelth.ziedbot.listeners.GuildMessageReactionAdd
import fr.ziedelth.ziedbot.listeners.SlashCommand
import fr.ziedelth.ziedbot.utils.*
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.EpisodeType
import fr.ziedelth.ziedbot.utils.animes.Language
import fr.ziedelth.ziedbot.utils.animes.News
import fr.ziedelth.ziedbot.utils.commands.Command
import fr.ziedelth.ziedbot.utils.tokens.DiscordToken
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import java.awt.Color
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.temporal.TemporalAccessor
import java.util.*
import kotlin.math.min

class DiscordClient : Client {
    lateinit var master: User
    var image: String? = null
    private val JDA: JDA = JDABuilder.createDefault(
        Const.GSON.fromJson(
            Files.readString(
                File("tokens", "discord.json").toPath(),
                Charset.defaultCharset()
            ), DiscordToken::class.java
        ).token
    ).build()
    private val commands: Array<Command> = arrayOf(AnimeCommand())

    init {
        JDA.presence.setPresence(OnlineStatus.IDLE, true)
        JDA.awaitReady()

        JDA.guilds.forEach {
            it.getZiedGuild()
            val commandUpdateAction: CommandListUpdateAction = it.updateCommands()

            commands.forEach { command ->
                run {
                    val commandData = CommandData(command.name, command.description)
                    command.options.forEach { option ->
                        commandData.addOption(
                            option.type,
                            option.name,
                            option.description,
                            option.required
                        )
                    }
                    commandUpdateAction.addCommands(commandData)
                }
            }

            commandUpdateAction.submit()
        }

        ZiedLogger.info("Connected to ${JDA.guilds.size} guild(s)!")
        image = JDA.selfUser.avatarUrl
        JDA.addEventListener(SlashCommand(commands), GuildMessageReactionAdd())
        JDA.retrieveUserById(132903783792377856L).queue { user -> master = user }
        JDA.presence.activity = Activity.playing("bugged with master")
        JDA.presence.setPresence(OnlineStatus.ONLINE, false)
    }

    override fun sendEpisode(episodes: Array<Episode>, new: Boolean) {
        val size = episodes.size

        if (new) {
            if (size <= 12) {
                episodes.forEach {
                    val embed = getEpisodeEmbed(it).build()

                    guilds.forEach { (_, ziedGuild) ->
                        if (ziedGuild.animeChannels.containsValue(it.language)) {
                            ziedGuild.animeChannels.filter { entry -> entry.value == it.language }
                                .forEach { (textChannelId, _) ->
                                    val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                                    textChannel?.sendMessageEmbeds(embed)
                                        ?.queue { message -> if (message != null) it.messages.add(message.idLong) }
                                }
                        }
                    }
                }
            } else {
                val a: MutableMap<Language, MutableList<String>> = mutableMapOf()
                val b: MutableMap<Language, StringBuilder> = mutableMapOf()

                episodes.forEach {
                    val animes: MutableList<String> = a.getOrDefault(it.language, mutableListOf())
                    val stringBuilder: StringBuilder = b.getOrDefault(it.language, StringBuilder())

                    if (!animes.contains(it.anime)) {
                        animes.add(it.anime)
                        a[it.language] = animes

                        val display = "• ${it.anime}"
                        val s = "• [** ${it.anime} **](${it.link})\n"

                        if (stringBuilder.length + display.length < 2000) {
                            stringBuilder.append(s)
                            b[it.language] = stringBuilder
                        }
                    }
                }

                Language.values().forEach {
                    if (a.containsKey(it) && b.containsKey(it)) {
                        val embed = setEmbed(
                            title = "$size episodes",
                            description = b[it]!!.toString(),
                            timestamp = Calendar.getInstance().toInstant()
                        ).build()

                        guilds.forEach { (_, ziedGuild) ->
                            if (ziedGuild.animeChannels.containsValue(it)) {
                                ziedGuild.animeChannels.filter { entry -> entry.value == it }
                                    .forEach { (textChannelId, _) ->
                                        val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                                        textChannel?.sendMessageEmbeds(embed)?.queue()
                                    }
                            }
                        }
                    }
                }
            }
        } else {
            episodes.forEach {
                val embed = getEpisodeEmbed(it).build()

                it.messages.forEach { messageId ->
                    guilds.forEach { (_, ziedGuild) ->
                        ziedGuild.guild.textChannels.forEach { textChannel ->
                            textChannel.retrieveMessageById(messageId).queue({ message ->
                                run {
                                    message.editMessageEmbeds(embed).queue()
                                }
                            }, { })
                        }
                    }
                }
            }
        }
    }

    override fun sendNews(news: Array<News>) {
        news.forEach {
            val embed = getNewsEmbed(it).build()

            guilds.forEach { (_, ziedGuild) ->
                if (ziedGuild.animeChannels.containsValue(it.language)) {
                    ziedGuild.animeChannels.filter { entry -> entry.value == it.language }
                        .forEach { (textChannelId, _) ->
                            val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                            textChannel?.sendMessageEmbeds(embed)?.queue()
                        }
                }
            }
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
        color: Color? = null,
        image: String? = null,
        footer: String? = null,
        timestamp: TemporalAccessor? = null
    ): EmbedBuilder {
        val embedBuilder = EmbedBuilder()
        embedBuilder.setAuthor(author, authorUrl, authorIcon)
        embedBuilder.setTitle(title, titleUrl)
        embedBuilder.setThumbnail(if (thumbnail == null || thumbnail.isEmpty()) master.avatarUrl else thumbnail)
        embedBuilder.setDescription(description)
        embedBuilder.setColor(color)
        embedBuilder.setImage(image)
        embedBuilder.setFooter(
            "${if (!footer.isNullOrEmpty()) "$footer  •  " else ""}Powered by Ziedelth.fr \uD83D\uDDA4",
            this.image
        )
        embedBuilder.setTimestamp(timestamp)
        return embedBuilder
    }

    private fun substring(string: String, int: Int): String {
        return string.substring(0, min(string.length, int))
    }

    private fun getEpisodeEmbed(episode: Episode): EmbedBuilder {
        return setEmbed(
            episode.p?.getName(),
            episode.p?.getURL(),
            episode.p?.getImage(),
            episode.anime,
            episode.link,
            description = """
                ${if (episode.title != null) "** ${episode.title} **" else ""}
                ${episode.language.episode} ${episode.number}
            """.trimIndent(),
            color = episode.p?.getColor(),
            image = episode.image,
            footer = if (episode.type == EpisodeType.SUBTITLES) episode.language.subtitles else episode.language.voice,
            timestamp = toCalendar(episode.calendar).toInstant()
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
            ** ${substring(news.description, 100)}... **
            
            ${substring(news.content, 1500)}...
            """.trimIndent(),
            color = news.p?.getColor(),
            timestamp = toCalendar(news.calendar).toInstant()
        )
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("HH:mm:ss yyyy/MM/dd").parse(s)
        calendar.time = date
        return calendar
    }
}