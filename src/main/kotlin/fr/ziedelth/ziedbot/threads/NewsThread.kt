package fr.ziedelth.ziedbot.threads

import fr.ziedelth.ziedbot.ZiedBot
import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.ZiedLogger
import fr.ziedelth.ziedbot.utils.animes.News
import net.dv8tion.jda.api.EmbedBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.math.min

class NewsThread(val ziedBot: ZiedBot) : Runnable {
    val thread = Thread(this, "NewsThread")
    val file = File("news.json")
    var list: MutableList<String> = mutableListOf()

    init {
        this.thread.isDaemon = true
        this.thread.start()
        if (this.file.exists()) this.list =
            Const.GSON.fromJson(Files.readString(this.file.toPath(), StandardCharsets.UTF_8), this.list::class.java)
    }

    override fun run() {
        while (!this.thread.isInterrupted) {
            val news: MutableList<News> = mutableListOf()
            Const.platforms.forEach { news.addAll(it.getLastNews()) }
            val l = news.filter { !this.list.contains(it.id) }
            val c = l.count()

            if (c > 0) {
                ZiedLogger.info("New $c new(s)!")

                l.forEach {
                    this.list.add(it.id)

                    this.ziedBot.jda.guilds.forEach { guild ->
                        guild.getTextChannelsByName("bot\uD83E\uDD16", true).firstOrNull()
                            ?.sendMessage(getNewsEmbed(it).build())?.queue()
                    }
                }


                Files.writeString(this.file.toPath(), Const.GSON.toJson(this.list), StandardCharsets.UTF_8)
            }

            this.thread.join(60000)
        }
    }

    private fun substring(string: String, int: Int): String {
        return string.substring(0, min(string.length, int))
    }

    fun getNewsEmbed(news: News): EmbedBuilder {
        return this.ziedBot.setEmbed(
            news.platform.getName(),
            news.platform.getURL(),
            news.platform.getImage(),
            news.title,
            news.link,
            null,
            """
            ** ${substring(news.description, 100)}... **
            
            ${substring(news.content, 1500)}...
            """.trimIndent(),
            news.platform.getColor(),
            null,
            news.calendar.toInstant()
        )
    }
}