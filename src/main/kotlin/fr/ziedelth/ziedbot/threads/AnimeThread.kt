package fr.ziedelth.ziedbot.threads

import com.google.gson.JsonArray
import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.animes.Anime
import fr.ziedelth.ziedbot.utils.animes.Episode
import net.dv8tion.jda.api.EmbedBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

class AnimeThread(val ziedBot: ZiedBot) : Runnable {
    val thread = Thread(this, "AnimeThread")
    val file = File("animes.json")
    var list: MutableList<Anime> = mutableListOf()

    init {
        this.thread.isDaemon = true
        this.thread.start()

        if (this.file.exists()) {
            val array: JsonArray =
                Const.GSON.fromJson(Files.readString(this.file.toPath(), StandardCharsets.UTF_8), JsonArray::class.java)
            array.filter { !it.isJsonNull && it.isJsonObject }
                .forEach { this.list.add(Const.GSON.fromJson(it, Anime::class.java)) }
        }
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            val episodes: MutableList<Episode> = mutableListOf()
            Const.platforms.forEach { episodes.addAll(it.getLastEpisodes()) }
            var c = 0

            episodes.forEach {
                if (this.contains(it.anime)) {
                    val anime = this.list.firstOrNull { anime -> anime.name == it.anime }

                    if (anime != null) {
                        if (anime.add(it)) {
                            c++
                            ziedBot.jda.guilds.forEach { guild ->
                                guild.getTextChannelsByName("bot\uD83E\uDD16", true).firstOrNull()
                                    ?.sendMessage(getEpisodeEmbed(it).build())
                                    ?.queue()
                            }
                        }
                    }
                } else {
                    val anime = Anime(it.anime)

                    if (anime.add(it)) {
                        c++
                        ziedBot.jda.guilds.forEach { guild ->
                            guild.getTextChannelsByName("bot\uD83E\uDD16", true).firstOrNull()
                                ?.sendMessage(getEpisodeEmbed(it).build())
                                ?.queue()
                        }
                    }

                    this.list.add(anime)
                }
            }

            if (c > 0) {
                ZiedLogger.info("New $c episode(s)!")
                Files.writeString(this.file.toPath(), Const.GSON.toJson(this.list), StandardCharsets.UTF_8)
            }

            this.thread.join(60000)
        }
    }

    fun contains(name: String?): Boolean = this.list.any { it.name == name }
    fun contains(anime: Anime?): Boolean = this.contains(anime?.name)

    private fun getEpisodeEmbed(episode: Episode): EmbedBuilder {
        return this.ziedBot.setEmbed(
            episode.p.getName(),
            episode.p.getURL(),
            episode.p.getImage(),
            episode.anime,
            episode.link,
            null,
            """
                ${if (episode.title != null) "** ${episode.title} **" else ""}
                Episode ${episode.number}
            """.trimIndent(),
            episode.p.getColor(),
            episode.image,
            toCalendar(episode.calendar).toInstant()
        )
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("HH:mm:ss yyyy/MM/dd", Locale.FRANCE).parse(s)
        calendar.time = date
        return calendar
    }
}
