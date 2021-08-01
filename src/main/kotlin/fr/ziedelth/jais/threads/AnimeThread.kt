package fr.ziedelth.jais.threads

import com.google.gson.JsonArray
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.News
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.math.max

class AnimeThread : Runnable {
    private val thread = Thread(this, "AnimeThread")
    private val episodesFile = File("episodes.json")
    private val newsFile = File("news.json")
    private var episodesList: MutableMap<String, Episode> = mutableMapOf()
    private var newsList: MutableList<News> = mutableListOf()

    init {
        this.thread.isDaemon = true
        this.thread.start()

        if (this.episodesFile.exists()) {
            val array: JsonArray =
                Const.GSON.fromJson(
                    Files.readString(this.episodesFile.toPath(), Const.DEFAULT_CHARSET),
                    JsonArray::class.java
                )

            array.filter { !it.isJsonNull && it.isJsonObject }.forEach {
                val episode = Const.GSON.fromJson(it, Episode::class.java)
                this.episodesList[episode.globalId] = episode
            }
        }

        if (this.newsFile.exists()) {
            val array: JsonArray =
                Const.GSON.fromJson(
                    Files.readString(this.newsFile.toPath(), Const.DEFAULT_CHARSET),
                    JsonArray::class.java
                )
            array.filter { !it.isJsonNull && it.isJsonObject }
                .forEach { this.newsList.add(Const.GSON.fromJson(it, News::class.java)) }
        }
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            JLogger.info("Checking news...")
            val checkStart = System.currentTimeMillis()

            var start = System.currentTimeMillis()

            val news: MutableList<News> = mutableListOf()
            Const.PLATFORMS.forEach { news.addAll(it.getLastNews()) }
            val newNews = news.stream().filter { !this.contains(it) }.collect(Collectors.toList())

            if (newNews.isNotEmpty()) {
                newNews.forEach { this.newsList.add(it) }
                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { it.sendNews(newNews.toTypedArray()) }
                JLogger.info("New ${newNews.size} news(s)!")
                Files.writeString(this.newsFile.toPath(), Const.GSON.toJson(this.newsList), Const.DEFAULT_CHARSET)
            }

            var end = System.currentTimeMillis()
            JLogger.info("Took ${(end - start)}ms to check news")
            JLogger.info("Checking episodes...")
            start = System.currentTimeMillis()

            val episodes: MutableList<Episode> = mutableListOf()

            Const.PLATFORMS.forEach {
                val startPlatformCheckTime = System.currentTimeMillis()
                episodes.addAll(it.getLastEpisodes())
                val endPlatformCheckTime = System.currentTimeMillis()
                JLogger.info("Took ${endPlatformCheckTime - startPlatformCheckTime}ms to check episodes on platform ${it.getName()} ")
            }

            val newEpisodes =
                episodes.stream().filter { !this.episodesList.containsKey(it.globalId) }.collect(Collectors.toList())
            val editEpisodes = episodes.stream()
                .filter {
                    this.episodesList.containsKey(it.globalId) && this.episodesList[it.globalId]!! != it && !this.episodesList[it.globalId]!!.datas.contains(
                        it.data
                    )
                }
                .collect(Collectors.toList())

            if (newEpisodes.isNotEmpty()) {
                newEpisodes.forEach {
                    it.datas.add(it.data)
                    this.episodesList[it.globalId] = it
                }

                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client ->
                    client.sendEpisode(
                        newEpisodes.toTypedArray(),
                        true
                    )
                }

                JLogger.info("New ${newEpisodes.size} episode(s)!")
                Files.writeString(
                    this.episodesFile.toPath(),
                    Const.GSON.toJson(this.episodesList.values),
                    Const.DEFAULT_CHARSET
                )
            }

            if (editEpisodes.isNotEmpty()) {
                val a: MutableList<Episode> = mutableListOf()

                editEpisodes.forEach {
                    val episode = this.episodesList[it.globalId]!!
                    episode.edit(it)
                    episode.datas.add(episode.data)
                    this.episodesList[it.globalId] = episode
                    a.add(episode)
                }

                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client -> client.sendEpisode(a.toTypedArray(), false) }
                JLogger.info("Edit ${editEpisodes.size} episode(s)!")
                Files.writeString(
                    this.episodesFile.toPath(),
                    Const.GSON.toJson(this.episodesList.values),
                    Const.DEFAULT_CHARSET
                )
            }

            end = System.currentTimeMillis()
            JLogger.info("Took ${(end - start)}ms to check episodes")

            val checkEnd = System.currentTimeMillis()
            val fullCheckTime = checkEnd - checkStart
            val waitingTimeToNextProcess = (Const.DELAY_BETWEEN_REQUEST * 60000) - fullCheckTime
            JLogger.warning("Took ${(fullCheckTime / 1000)}s to full check!")
            JLogger.warning(
                "Need to wait ${
                    (max(
                        0,
                        waitingTimeToNextProcess
                    )).toDouble() / 1000.0
                }s to do the next check! ${if (waitingTimeToNextProcess < 0) "OVERLOAD" else ""}"
            )
            this.thread.join(if (waitingTimeToNextProcess < 0) 1 else waitingTimeToNextProcess)
        }
    }

    fun contains(string: String): Boolean = this.newsList.any { it.toString() == string }
    fun contains(news: News): Boolean = this.contains(news.toString())
}