package fr.ziedelth.ziedbot.threads.animes

import com.google.gson.JsonArray
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.EpisodeType
import fr.ziedelth.ziedbot.utils.animes.Language
import fr.ziedelth.ziedbot.utils.guilds
import net.dv8tion.jda.api.EmbedBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

class AnimeEpisodesThread : Runnable {
    private val thread = Thread(this, "AnimeThread")
    private val file = File("animes.json")
    private var dataset: MutableMap<String, Episode> = mutableMapOf()

    init {
        this.thread.isDaemon = true
        this.thread.start()

        if (this.file.exists()) {
            val array: JsonArray =
                Const.GSON.fromJson(Files.readString(this.file.toPath(), StandardCharsets.UTF_8), JsonArray::class.java)

            array.filter { !it.isJsonNull && it.isJsonObject }.forEach {
                val episode = Const.GSON.fromJson(it, Episode::class.java)
                dataset[episode.globalId] = episode
            }
        }
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            val episodes: MutableList<Episode> = mutableListOf()
            Const.PLATFORMS.forEach { episodes.addAll(it.getLastEpisodes()) }

            val newEpisodes =
                episodes.stream().filter { !this.dataset.containsKey(it.globalId) }.collect(Collectors.toList())
            val editEpisodes =
                episodes.stream().filter { this.dataset.containsKey(it.globalId) && this.dataset[it.globalId]!! != it }
                    .collect(Collectors.toList())

            if (newEpisodes.isNotEmpty()) {
                val size = newEpisodes.size

                if (size <= 12) {
                    newEpisodes.forEach {
                        if (Const.SEND_MESSAGES) {
                            guilds.forEach { (_, ziedGuild) ->
                                if (ziedGuild.animeChannels.containsKey(it.language)) {
                                    val textChannel =
                                        ziedGuild.guild.getTextChannelById(ziedGuild.animeChannels[it.language]!!)
                                    val message =
                                        textChannel?.sendMessageEmbeds(getEpisodeEmbed(it).build())?.complete()
                                    if (message != null) it.messages.add(message.idLong)
                                }
                            }
                        }

                        this.dataset[it.globalId] = it
                    }
                } else {
                    val a: MutableMap<Language, MutableList<String>> = mutableMapOf()
                    val b: MutableMap<Language, StringBuilder> = mutableMapOf()

                    newEpisodes.forEach {
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

                        this.dataset[it.globalId] = it
                    }

                    if (Const.SEND_MESSAGES) {
                        Language.values().forEach {
                            if (a.containsKey(it) && b.containsKey(it)) {
                                guilds.forEach { (_, ziedGuild) ->
                                    if (ziedGuild.animeChannels.containsKey(it)) {
                                        val textChannel =
                                            ziedGuild.guild.getTextChannelById(ziedGuild.animeChannels[it]!!)
                                        textChannel?.sendMessageEmbeds(
                                            Const.setEmbed(
                                                title = "$size episodes",
                                                description = b[it]!!.toString(),
                                                timestamp = Calendar.getInstance().toInstant()
                                            ).build()
                                        )?.queue()
                                    }
                                }
                            }
                        }
                    }
                }

                ZiedLogger.info("New $size episode(s)!")
                Files.writeString(this.file.toPath(), Const.GSON.toJson(this.dataset.values), StandardCharsets.UTF_8)
            }

            if (editEpisodes.isNotEmpty()) {
                val size = editEpisodes.size

                editEpisodes.forEach {
                    val episode = this.dataset[it.globalId]!!

                    episode.id = it.id
                    episode.title = it.title
                    episode.image = it.image
                    episode.link = it.link

                    this.dataset[it.globalId] = episode

                    if (Const.SEND_MESSAGES) {
                        episode.messages.forEach { messageId ->
                            guilds.forEach { (_, ziedGuild) ->
                                ziedGuild.guild.textChannels.forEach { textChannel ->
                                    textChannel.retrieveMessageById(messageId).queue({ message ->
                                        run {
                                            message.editMessageEmbeds(getEpisodeEmbed(it).build()).queue()
                                        }
                                    }, { })
                                }
                            }
                        }
                    }
                }

                ZiedLogger.info("Edit $size episode(s)!")
                Files.writeString(this.file.toPath(), Const.GSON.toJson(this.dataset.values), StandardCharsets.UTF_8)
            }

            this.thread.join(Const.DELAY_BETWEEN_REQUEST * 60000)
        }
    }

    private fun getEpisodeEmbed(episode: Episode): EmbedBuilder {
        return Const.setEmbed(
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

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("HH:mm:ss yyyy/MM/dd").parse(s)
        calendar.time = date
        return calendar
    }
}
