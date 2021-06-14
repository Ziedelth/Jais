package fr.ziedelth.ziedbot.threads.animes

import com.google.gson.JsonArray
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.EpisodeType
import fr.ziedelth.ziedbot.utils.guilds
import net.dv8tion.jda.api.EmbedBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

class AnimeEpisodesThread : Runnable {
    private val thread = Thread(this, "AnimeThread")
    private val file = File("animes.json")
    private var list: MutableList<Episode> = mutableListOf()

    init {
        this.thread.isDaemon = true
        this.thread.start()

        if (this.file.exists()) {
            val array: JsonArray =
                Const.GSON.fromJson(Files.readString(this.file.toPath(), StandardCharsets.UTF_8), JsonArray::class.java)
            array.filter { !it.isJsonNull && it.isJsonObject }
                .forEach { this.list.add(Const.GSON.fromJson(it, Episode::class.java)) }
        }
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            val episodes: MutableList<Episode> = mutableListOf()
            Const.PLATFORMS.forEach { episodes.addAll(it.getLastEpisodes()) }
            var c = 0

            episodes.forEach {
                if (this.contains(it)) {
                    val episode = this.list.find { episode -> episode.globalId == it.globalId }

                    if (episode != null) {
                        if (!episode.equals(it)) {
                            c++
                            val eIndex = this.list.indexOf(episode)

                            episode.id = it.id
                            episode.title = it.title
                            episode.image = it.image
                            episode.link = it.link

                            episode.messages.forEach { message ->
                                run {
                                    message?.editMessage(getEpisodeEmbed(it).build())?.queue()
                                }
                            }

                            this.list[eIndex] = episode
                        }
                    }
                } else {
                    c++

                    guilds.forEach { (_, ziedGuild) ->
                        ziedGuild.animeChannel?.sendMessage(getEpisodeEmbed(it).build())?.queue { message ->
                            run {
                                val episode = this.list.find { it1 -> it1.globalId == it.globalId }

                                if (episode != null) {
                                    val eIndex = this.list.indexOf(episode)
                                    episode.messages.add(message)
                                    this.list[eIndex] = episode
                                }
                            }
                        }
                    }

                    this.list.add(it)
                }
            }

            if (c > 0) {
                ZiedLogger.info("New $c episode(s)!")
                Files.writeString(this.file.toPath(), Const.GSON.toJson(this.list), StandardCharsets.UTF_8)
            }

            this.thread.join(Const.DELAY_BETWEEN_REQUEST * 60000)
        }
    }

    fun contains(globalId: String?): Boolean = this.list.any { it.globalId == globalId }
    fun contains(episode: Episode?): Boolean = this.contains(episode?.globalId)

    private fun getEpisodeEmbed(episode: Episode): EmbedBuilder {
        return Const.setEmbed(
            episode.p?.getName(),
            episode.p?.getURL(),
            episode.p?.getImage(),
            episode.anime,
            episode.link,
            description = """
                ${if (episode.title != null) "** ${episode.title} **" else ""}
                Episode ${episode.number}
            """.trimIndent(),
            color = episode.p?.getColor(),
            image = episode.image,
            footer = if (episode.type == EpisodeType.SUBTITLES) "VOSTFR" else "VF",
            timestamp = toCalendar(episode.calendar).toInstant()
        )
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("HH:mm:ss yyyy/MM/dd", Locale.FRANCE).parse(s)
        calendar.time = date
        return calendar
    }
}
