/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.clients

import fr.ziedelth.jais.commands.ClearCommand
import fr.ziedelth.jais.commands.ConfigCommand
import fr.ziedelth.jais.commands.PlatformsCommand
import fr.ziedelth.jais.listeners.*
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.Emoji.CLAP
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.clients.Client
import fr.ziedelth.jais.utils.clients.JClient
import fr.ziedelth.jais.utils.commands.Command
import fr.ziedelth.jais.utils.tokens.DiscordToken
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import java.awt.Color
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.temporal.TemporalAccessor
import java.util.concurrent.CompletableFuture

class DiscordClient : Client {
    private val file = File("discord.json")
    private val tokenFile = File(Const.TOKENS_FOLDER, "discord.json")
    private val init: Boolean
    private var jda: JDA? = null
    private var master: User? = null
    private var image: String? = null
    private val commands: Array<Command> = arrayOf(PlatformsCommand(), ClearCommand(), ConfigCommand())

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

                this.jda!!.guilds.forEach {
                    it.getJGuild()

                    if (!Const.PUBLIC) {
                        val clua = it.updateCommands()

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

                                clua.addCommands(commandData)
                            }
                        }

                        clua.submit()
                    } else it.updateCommands().submit()
                }

                if (Const.PUBLIC) {
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
            }
        }
    }

    override fun getJClient(): JClient {
        return if (this.file.exists()) Const.GSON.fromJson(
            Files.readString(this.file.toPath(), Const.DEFAULT_CHARSET),
            JClient::class.java
        ) else JClient()
    }

    override fun saveJClient(jClient: JClient) {
        Files.writeString(this.file.toPath(), Const.GSON.toJson(jClient), Const.DEFAULT_CHARSET)
    }

    override fun update() {
        JLogger.info("[${this.javaClass.simpleName}] Connected to ${this.jda!!.guilds.size} guild(s)!")
        this.image = this.jda!!.selfUser.avatarUrl
        this.jda!!.retrieveUserById(132903783792377856L).queue { user -> master = user }
        this.jda!!.presence.activity = Activity.playing("bugged with master")
        this.jda!!.presence.setPresence(OnlineStatus.ONLINE, false)
    }

    override fun sendNewEpisodes(episodes: Array<Episode>) {
        if (!this.init || episodes.isEmpty()) return
        val jClient = this.getJClient()
        val list: MutableList<CompletableFuture<Message>> = mutableListOf()

        episodes.forEach { episode ->
            val jEpisode = jClient.getEpisodeById(Const.toId(episode))
            val embed = getEpisodeEmbed(episode).build()

            guilds.values.forEach { ziedGuild ->
                ziedGuild.animeChannels.filter { (_, channel) -> channel.countries.contains(episode.country) }
                    .forEach { (textChannelId, _) ->
                        val textChannel = ziedGuild.guild.getTextChannelById(textChannelId)
                        if (textChannel != null) list.add(
                            textChannel.sendMessageEmbeds(embed).submit().whenComplete { message, _ ->
                                jEpisode.messages.add(message.idLong); jClient.addEpisode(jEpisode)
                            })
                    }
            }
        }

        CompletableFuture.allOf(*list.toTypedArray()).whenComplete { _, _ ->
            JLogger.info("Saving ${list.size} messages for ${episodes.size} episodes...")
            this.saveJClient(jClient)
        }
    }

    override fun sendEditEpisodes(episodes: Array<Episode>) {
        if (!this.init || episodes.isEmpty()) return
        val jClient = this.getJClient()

        episodes.forEach { episode ->
            val jEpisode = jClient.getEpisodeById(Const.toId(episode))
            val embed = getEpisodeEmbed(episode).build()

            if (jClient.hasEpisode(Const.toId(episode))) {
                jEpisode.messages.forEach { messageId ->
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
        if (!this.init || news.isEmpty()) return

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
            episode.platform.getName(),
            episode.platform.getURL(),
            episode.platform.getImage(),
            episode.anime,
            episode.url,
            description = """
                ${if (episode.title != null) "** ${episode.title} **" else ""}
                ${episode.season} • ${episode.country.episode} ${episode.number}
                ${if (episode.duration > 0) "$CLAP ${SimpleDateFormat("mm:ss").format(episode.duration * 1000)}" else ""}
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