package fr.ziedelth.jais.utils.animes

import com.google.gson.JsonArray
import fr.ziedelth.jais.utils.Const
import java.io.File
import java.nio.file.Files

private val file = File("news.json")

fun getNews(): MutableList<News> {
    val news: MutableList<News> = mutableListOf()

    if (file.exists()) {
        val array: JsonArray =
            Const.GSON.fromJson(
                Files.readString(file.toPath(), Const.DEFAULT_CHARSET),
                JsonArray::class.java
            )
        array.filter { !it.isJsonNull && it.isJsonObject }
            .forEach { news.add(Const.GSON.fromJson(it, News::class.java)) }
    }

    return news
}

fun saveNews(news: Collection<News>) {
    Files.writeString(file.toPath(), Const.GSON.toJson(news), Const.DEFAULT_CHARSET)
}

class News(
    val platform: String,
    val calendar: String,
    val title: String,
    val description: String,
    val content: String,
    val link: String,
    val country: Country
) {
    @Transient
    var p: Platform? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as News

        if (platform != other.platform) return false
        if (calendar != other.calendar) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (content != other.content) return false
        if (link != other.link) return false
        if (country != other.country) return false

        return true
    }

    override fun toString(): String {
        return "News(platform='$platform', calendar='$calendar', language=$country)"
    }

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + country.hashCode()
        return result
    }
}