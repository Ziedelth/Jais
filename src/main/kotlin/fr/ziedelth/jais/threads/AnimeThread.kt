package fr.ziedelth.jais.threads

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.*
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
            val checkStart = System.currentTimeMillis()
            val episodesList: MutableMap<String, Episode> = getEpisodes()
            val newsList: MutableList<News> = getNews()

            val news: MutableList<News> = mutableListOf()

            Const.PLATFORMS.forEach {
                news.addAll(it.getLastNews())
            }

            val newNews = news.stream().filter { !this.contains(newsList, it) }.collect(Collectors.toList())

            if (newNews.isNotEmpty()) {
                newNews.forEach { newsList.add(it) }
                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { it.sendNews(newNews.toTypedArray()) }
                saveNews(newsList)
            }

            val episodes: MutableList<Episode> = mutableListOf()

            Const.PLATFORMS.forEach {
                episodes.addAll(it.getLastEpisodes())
            }

            val newEpisodes =
                episodes.stream().filter { !episodesList.containsKey(it.globalId) }.collect(Collectors.toList())
            val editEpisodes = episodes.stream()
                .filter {
                    episodesList.containsKey(it.globalId) && episodesList[it.globalId]!! != it && !episodesList[it.globalId]!!.datas.contains(
                        it.data
                    )
                }
                .collect(Collectors.toList())

            if (newEpisodes.isNotEmpty()) {
                newEpisodes.forEach {
                    it.datas.add(it.data)
                    episodesList[it.globalId] = it
                }

                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client ->
                    client.sendEpisode(
                        newEpisodes.toTypedArray(),
                        true
                    )
                }

                saveEpisodes(episodesList.values)
            }

            if (editEpisodes.isNotEmpty()) {
                val a: MutableList<Episode> = mutableListOf()

                editEpisodes.forEach {
                    val episode = episodesList[it.globalId]!!
                    episode.edit(it)
                    episode.datas.add(episode.data)
                    episodesList[it.globalId] = episode
                    a.add(episode)
                }

                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client -> client.sendEpisode(a.toTypedArray(), false) }

                saveEpisodes(episodesList.values)
            }

            val checkEnd = System.currentTimeMillis()
            val fullCheckTime = checkEnd - checkStart
            val waitingTimeToNextProcess = (Const.DELAY_BETWEEN_REQUEST * 60000) - fullCheckTime

            JLogger.info(
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