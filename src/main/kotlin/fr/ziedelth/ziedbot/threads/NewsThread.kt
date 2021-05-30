package fr.ziedelth.ziedbot.threads

import com.google.gson.JsonArray
import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.animes.News
import net.dv8tion.jda.api.EmbedBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class NewsThread(val ziedBot: ZiedBot) : Runnable {
    val thread = Thread(this, "NewsThread")
    val file = File("news.json")
    var list: MutableList<News> = mutableListOf()

    init {
        this.thread.isDaemon = true
        this.thread.start()

        if (this.file.exists()) {
            val array: JsonArray =
                Const.GSON.fromJson(Files.readString(this.file.toPath(), StandardCharsets.UTF_8), JsonArray::class.java)
            array.filter { !it.isJsonNull && it.isJsonObject }
                .forEach { this.list.add(Const.GSON.fromJson(it, News::class.java)) }
        }
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            val news: MutableList<News> = mutableListOf()
            Const.platforms.forEach { news.addAll(it.getLastNews()) }
            var c = 0

            news.forEach {
                if (!this.contains(it)) {
                    c++
                    ziedBot.jda.guilds.forEach { guild ->
                        guild.getTextChannelsByName("bot\uD83E\uDD16", true).firstOrNull()
                            ?.sendMessage(getNewsEmbed(it).build())
                            ?.queue()
                    }

                    this.list.add(it)
                }
            }

            if (c > 0) {
                ZiedLogger.info("New $c news(s)!")
                Files.writeString(this.file.toPath(), Const.GSON.toJson(this.list), StandardCharsets.UTF_8)
            }

            this.thread.join(60000)
        }
    }

    fun contains(string: String): Boolean = this.list.any { it.toString() == string }
    fun contains(news: News): Boolean = this.contains(news.toString())

    private fun substring(string: String, int: Int): String {
        return string.substring(0, min(string.length, int))
    }

    fun getNewsEmbed(news: News): EmbedBuilder {
        return this.ziedBot.setEmbed(
            news.p.getName(),
            news.p.getURL(),
            news.p.getImage(),
            news.title,
            news.link,
            null,
            """
            ** ${substring(news.description, 100)}... **
            
            ${substring(news.content, 1500)}...
            """.trimIndent(),
            news.p.getColor(),
            null,
            toCalendar(news.calendar).toInstant()
        )
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("HH:mm:ss yyyy/MM/dd", Locale.FRANCE).parse(s)
        calendar.time = date
        return calendar
    }
}