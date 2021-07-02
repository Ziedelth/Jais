package fr.ziedelth.ziedbot.threads

import com.google.gson.JsonArray
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.News
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors

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
            val news: MutableList<News> = mutableListOf()
            Const.PLATFORMS.forEach { news.addAll(it.getLastNews()) }
            val newNews = news.stream().filter { !this.contains(it) }.collect(Collectors.toList())

            if (newNews.isNotEmpty()) {
                newNews.forEach { this.newsList.add(it) }
                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { it.sendNews(newNews.toTypedArray()) }
                ZiedLogger.info("New ${newNews.size} news(s)!")
                Files.writeString(this.newsFile.toPath(), Const.GSON.toJson(this.newsList), Const.DEFAULT_CHARSET)
            }

            val episodes: MutableList<Episode> = mutableListOf()
            Const.PLATFORMS.forEach { episodes.addAll(it.getLastEpisodes()) }

            val newEpisodes =
                episodes.stream().filter { !this.episodesList.containsKey(it.globalId) }.collect(Collectors.toList())
            val editEpisodes = episodes.stream()
                .filter { this.episodesList.containsKey(it.globalId) && this.episodesList[it.globalId]!! != it }
                .collect(Collectors.toList())

            if (newEpisodes.isNotEmpty()) {
                newEpisodes.forEach { this.episodesList[it.globalId] = it }
                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client ->
                    client.sendEpisode(
                        newEpisodes.toTypedArray(),
                        true
                    )
                }
                ZiedLogger.info("New ${newEpisodes.size} episode(s)!")
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

                    episode.id = it.id
                    episode.title = it.title
                    episode.image = it.image
                    episode.link = it.link
                    episode.p = it.p

                    this.episodesList[it.globalId] = episode
                    a.add(episode)
                }

                if (Const.SEND_MESSAGES) Const.CLIENTS.forEach { client -> client.sendEpisode(a.toTypedArray(), false) }
                ZiedLogger.info("Edit ${editEpisodes.size} episode(s)!")
                Files.writeString(
                    this.episodesFile.toPath(),
                    Const.GSON.toJson(this.episodesList.values),
                    Const.DEFAULT_CHARSET
                )
            }

            this.thread.join(Const.DELAY_BETWEEN_REQUEST * 60000)
        }
    }

    fun contains(string: String): Boolean = this.newsList.any { it.toString() == string }
    fun contains(news: News): Boolean = this.contains(news.toString())
}