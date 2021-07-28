package fr.ziedelth.jais.clients

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.commands.AnimeCommand
import fr.ziedelth.jais.listeners.GuildMessageReactionAdd
import fr.ziedelth.jais.listeners.SlashCommand
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.tokens.DiscordToken
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
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.temporal.TemporalAccessor
import java.util.*

class DiscordClient : Client {
    private val file = File("discord.json")
    private val obj: JsonObject = if (!this.file.exists()) JsonObject() else Const.GSON.fromJson(
        Files.readString(
            this.file.toPath(),
            Const.DEFAULT_CHARSET
        ), JsonObject::class.java
    )
    private val tokenFile = File(Const.TOKENS_FOLDER, "discord.json")
    private val init: Boolean
    private var jda: JDA? = null
    private var master: User? = null
    private var image: String? = null
    private val commands: Array<Command> = arrayOf(AnimeCommand())

    init {
        if (!this.tokenFile.exists()) {
            this.init = false
            JLogger.warning("Discord token not exists!")
            Files.writeString(this.tokenFile.toPath(), Const.GSON.toJson(DiscordToken()), Const.DEFAULT_CHARSET)
        } else {
            val discordToken = Const.GSON.fromJson(
                Files.readString(
                    this.tokenFile.toPath(),
                    Const.DEFAULT_CHARSET
                ), DiscordToken::class.java
            )

            if (discordToken.token.isEmpty()) {
                this.init = false
                JLogger.warning("Discord token is empty!")
            } else {
                this.init = true
                this.jda = JDABuilder.createDefault(discordToken.token).build()
                this.jda!!.presence.setPresence(OnlineStatus.IDLE, true)
                this.jda!!.awaitReady()

                this.jda!!.guilds.forEach { it.getJGuild() }

                val commandUpdateAction: CommandListUpdateAction = this.jda!!.updateCommands()

                this.commands.forEach { command ->
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

                this.jda!!.addEventListener(SlashCommand(this.commands), GuildMessageReactionAdd())
                this.update()
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

    override fun sendEpisode(episodes: Array<Episode>, new: Boolean) {
        if (!this.init) return
        val episodesObj: JsonObject = this.obj["episodes"]?.asJsonObject ?: JsonObject()

        if (new) {
            Country.values().forEach { country ->
                val countryEpisodes = episodes.filter { it.country == country }
                val size = countryEpisodes.size

                if (size <= 12) {
                    countryEpisodes.forEach {
                        val messageArray = episodesObj[it.globalId]?.asJsonArray ?: JsonArray()
                        val embed = getEpisodeEmbed(it).build()

                        guilds.forEach { (_, ziedGuild) ->
                            ziedGuild.animeChannels.filter { (_, channel) -> channel.countries.contains(country) }
                                .forEach { (textChannelId, _) ->
                                    val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                                    val message = textChannel?.sendMessageEmbeds(embed)?.complete()
                                    if (message != null) messageArray.add(message.idLong)
                                }
                        }

                        episodesObj.add(it.globalId, messageArray)
                    }

                    if (size > 0) {
                        this.obj.add("episodes", episodesObj)
                        Files.writeString(this.file.toPath(), Const.GSON.toJson(this.obj), Const.DEFAULT_CHARSET)
                    }
                } else {
                    val animes: MutableList<String> = mutableListOf()
                    val stringBuilder: StringBuilder = StringBuilder()
                    var i = 0
                    var image: String? = ""

                    countryEpisodes.forEach {
                        if (!animes.contains(it.anime)) {
                            val length = countryEpisodes.filter { episode -> episode.anime == it.anime }.size

                            if (length > i) {
                                i = length
                                image = it.image
                            }

                            animes.add(it.anime)

                            val display = "• ${it.anime}"
                            val s = "• [${it.anime}](${it.link})\n"

                            if (stringBuilder.length + display.length < 2000) stringBuilder.append(s)
                        }
                    }

                    val embed = setEmbed(
                        title = "${country.flag}  ${country.countryName}\n$size ${country.episode}s",
                        description = stringBuilder.toString(),
                        image = image,
                        timestamp = Calendar.getInstance().toInstant()
                    ).build()

                    guilds.forEach { (_, ziedGuild) ->
                        ziedGuild.animeChannels.filter { (_, channel) -> channel.countries.contains(country) }
                            .forEach { (textChannelId, _) ->
                                val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                                textChannel?.sendMessageEmbeds(embed)?.queue()
                            }
                    }
                }
            }
        } else {
            episodes.forEach {
                if (episodesObj.has(it.globalId)) {
                    val messageArray = episodesObj[it.globalId]!!.asJsonArray
                    val embed = getEpisodeEmbed(it).build()

                    messageArray.forEach { messageId ->
                        guilds.forEach { (_, ziedGuild) ->
                            ziedGuild.guild.textChannels.forEach { textChannel ->
                                textChannel.retrieveMessageById(messageId.asLong).queue({ message ->
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
    }

    override fun sendNews(news: Array<News>) {
        if (!this.init) return

        news.forEach {
            val embed = getNewsEmbed(it).build()

            guilds.forEach { (_, ziedGuild) ->
                ziedGuild.animeChannels.filter { (_, channel) -> channel.countries.contains(it.country) }
                    .forEach { (textChannelId, _) ->
                        val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                        textChannel?.sendMessageEmbeds(embed)?.queue()
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
            episode.p?.getName(),
            episode.p?.getURL(),
            episode.p?.getImage(),
            episode.anime,
            episode.link,
            description = """
                ${if (episode.title != null) "** ${episode.title} **" else ""}
                ${episode.country.episode} ${episode.number}
                🎬 ${SimpleDateFormat("mm:ss").format(episode.duration * 1000)}
            """.trimIndent(),
            color = episode.p?.getColor(),
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
            ** ${Const.substring(news.description, 450)}... **
            
            ${Const.substring(news.content, 1500)}...
            """.trimIndent(),
            color = news.p?.getColor(),
            timestamp = ISO8601.toCalendar(news.calendar).toInstant()
        )
    }
}