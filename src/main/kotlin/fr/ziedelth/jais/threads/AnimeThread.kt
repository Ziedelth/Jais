/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.threads

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JTime
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import fr.ziedelth.jais.utils.animes.getNews
import fr.ziedelth.jais.utils.animes.saveNews
import fr.ziedelth.jais.utils.database.JAccess
import java.util.concurrent.Executors
import kotlin.math.max

class AnimeThread : Runnable {
    private val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val thread = Thread(this, "AnimeThread")

    init {
        this.thread.isDaemon = true
        this.thread.start()
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            JTime.start("JT-Anime", "Starting detection...")
            val newsList: MutableList<News> = getNews()

            // NEWS
            val news: Array<News> =
                Const.PLATFORMS.map { it.getLastNews().toList() }.flatten().filter { !this.contains(newsList, it) }
                    .sortedBy { ISO8601.toCalendar(it.calendar) }
                    .toTypedArray()

            if (news.isNotEmpty()) {
                news.forEach { newsList.add(it) }
                if (Const.SEND_MESSAGES) this.pool.submit { Const.CLIENTS.forEach { it.sendNews(news) } }
                saveNews(newsList)
            }

            // EPISODES
            val connection = JAccess.getConnection()
            val episodes: Array<Episode> = Const.PLATFORMS.map { it.getLastEpisodes().toList() }.flatten()
                .filter { !JAccess.isExists(connection, it) }.sortedBy { ISO8601.toCalendar(it.calendar) }
                .toTypedArray()

            if (episodes.isNotEmpty()) {
                JTime.start("JT-Saving", "Saving all episodes")
                JAccess.insertEpisodes(connection, episodes)
                JTime.end("JT-Saving", "All episodes are saved. Takes %{ms}ms.")

                if (Const.SEND_MESSAGES) this.pool.submit { Const.CLIENTS.forEach { it.sendEpisodes(episodes) } }
            }

            connection?.close()
            val waitingTimeToNextProcess = (Const.DELAY_BETWEEN_REQUEST * 60000) - JTime.end(
                "JT-Anime",
                "Took %{s}s to full check! Need to wait %{ds}s to do the next check!",
                (Const.DELAY_BETWEEN_REQUEST * 60000)
            )
            System.gc()
            this.thread.join(max(1, waitingTimeToNextProcess))
        }
    }

    fun contains(newsList: Collection<News>, string: String): Boolean = newsList.any { it.toString() == string }
    fun contains(newsList: Collection<News>, news: News): Boolean = this.contains(newsList, news.toString())
}
