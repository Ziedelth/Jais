package fr.ziedelth.jais.threads

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.JTime
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.animes.getNews
import fr.ziedelth.jais.utils.animes.saveNews
import fr.ziedelth.jais.utils.database.JAccess
import java.util.stream.Collectors
import kotlin.math.max

class AnimeThread : Runnable {
    private val thread = Thread(this, "AnimeThread")

    init {
        this.thread.isDaemon = true
        this.thread.start()
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            JTime.start("JT-Anime")
            val newsList: MutableList<News> = getNews()

            // NEWS
            val news: MutableList<News> = mutableListOf()

            Const.PLATFORMS.forEach { news.addAll(it.getLastNews()) }

            val newNews = news.stream().filter { !this.contains(newsList, it) }.collect(Collectors.toList())

            if (newNews.isNotEmpty()) {
                newNews.forEach { newsList.add(it) }
                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { it.sendNews(newNews.toTypedArray()) }
                saveNews(newsList)
            }

            // EPISODES
            val connection = JAccess.getConnection()
            val episodes: MutableList<Episode> = mutableListOf()
            val newEpisodes: MutableList<Episode> = mutableListOf()
            val editEpisodes: MutableList<Episode> = mutableListOf()

            Const.PLATFORMS.forEach { platform ->
                JTime.start("JT-Platform", "Starting episodes detection for ${platform.getName()}...")
                episodes.addAll(platform.getLastEpisodes())
                JTime.end(
                    "JT-Platform",
                    "Finish episodes detection for ${platform.getName()}. Takes %ms to full detect."
                )
            }

            if (episodes.isNotEmpty()) {
                JTime.start("JT-Saving", "Saving all episodes")
                episodes.sortedBy { ISO8601.toCalendar(it.calendar) }.forEach { episode ->
                    // NEW
                    if (JAccess.getJEpisode(
                            connection,
                            episode.platform.getName(),
                            episode.country.country,
                            episode.anime,
                            episode.season,
                            episode.number,
                            episode.type.name
                        ) == null
                    ) {
                        newEpisodes.add(episode)

                        val jPlatform = JAccess.insertPlatform(connection, episode.platform) ?: return@forEach
                        val jCountry = JAccess.insertCountry(connection, episode.country.country) ?: return@forEach
                        val jAnime = JAccess.insertAnime(connection, episode.calendar, jCountry, episode.anime, null)
                            ?: return@forEach
                        val jSeason =
                            JAccess.insertSeason(connection, episode.calendar, jAnime, episode.season) ?: return@forEach
                        val jNumber = JAccess.insertNumber(connection, episode.calendar, jSeason, episode.number)
                            ?: return@forEach
                        JAccess.insertEpisode(
                            connection,
                            episode.calendar,
                            jPlatform,
                            jNumber,
                            episode.type.name,
                            episode.episodeId,
                            episode.title,
                            episode.image,
                            episode.url,
                            episode.duration
                        )
                    }
                    // EDIT
                    else {
                        val jEpisode = JAccess.getJEpisode(
                            connection,
                            episode.platform.getName(),
                            episode.country.country,
                            episode.anime,
                            episode.season,
                            episode.number,
                            episode.type.name
                        ) ?: return@forEach

                        if (episode.episodeId != jEpisode.episodeId ||
                            episode.title != jEpisode.title ||
                            episode.image != jEpisode.image ||
                            episode.url != jEpisode.url ||
                            episode.duration != jEpisode.duration
                        ) {
                            editEpisodes.add(episode)
                            JLogger.warning("Edit $episode > $jEpisode")

                            JAccess.updateEpisode(
                                connection,
                                jEpisode,
                                episode.episodeId,
                                episode.title,
                                episode.image,
                                episode.url,
                                episode.duration
                            )
                        }
                    }
                }
                JTime.end("JT-Saving", "All episodes are saved. Takes %ms.")

                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client ->
                    client.sendNewEpisodes(newEpisodes.toTypedArray())
                    client.sendEditEpisodes(editEpisodes.toTypedArray())
                }
            }

            connection?.close()
            val fullCheckTime = JTime.end("JT-Anime")
            val waitingTimeToNextProcess = (Const.DELAY_BETWEEN_REQUEST * 60000) - fullCheckTime

            JLogger.warning(
                "Took ${(fullCheckTime / 1000)}s to full check! Need to wait ${
                    (max(
                        0,
                        waitingTimeToNextProcess
                    )).toDouble() / 1000.0
                }s to do the next check! ${if (waitingTimeToNextProcess <= 0) "OVERLOAD" else ""}"
            )

            this.thread.join(if (waitingTimeToNextProcess <= 0) 1 else waitingTimeToNextProcess)
        }
    }

    fun contains(newsList: Collection<News>, string: String): Boolean = newsList.any { it.toString() == string }
    fun contains(newsList: Collection<News>, news: News): Boolean = this.contains(newsList, news.toString())
}