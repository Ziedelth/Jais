package fr.ziedelth.ziedbot.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.ziedbot.utils.Request
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.News
import fr.ziedelth.ziedbot.utils.animes.Platform
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class AnimeDigitalNetwork : Platform {
    override fun getName(): String = "Anime Digital Network"
    override fun getURL(): String = "https://animedigitalnetwork.fr/"
    override fun getImage(): String = "https://img.apksum.com/e6/fr.anidn/v4.1.65/icon.png"
    override fun getColor(): Color = Color(0, 150, 255)

    override fun getLastNews(): MutableList<News> = mutableListOf()

    override fun getLastEpisodes(): MutableList<Episode> {
        val l: MutableList<Episode> = mutableListOf()
        val jsonObject = Gson().fromJson(
            Request.get("https://gw.api.animedigitalnetwork.fr/video/calendar?date=${this.nowDate()}"),
            JsonObject::class.java
        )
        val jsonArray = jsonObject.getAsJsonArray("videos")
        val calendar = Calendar.getInstance()

        jsonArray.filter { it.isJsonObject }.forEach {
            val jObject = it.asJsonObject
            val showObject = jObject.getAsJsonObject("show")
            val link = jObject.get("url").asString.replace(" ", "%20")
            val anime = showObject.get("title").asString
            val title: String? =
                if (jObject.has("name") && !jObject.get("name").isJsonNull) jObject.get("name").asString else null
            val image = jObject.get("image2x").asString.replace(" ", "%20")
            val number = jObject.get("shortNumber").asString.toInt()
            val releaseDate = this.toCalendar(jObject.get("releaseDate").asString)

            if (calendar.after(releaseDate)) {
                val episode = Episode(this.getName(), toStringCalendar(releaseDate), title, image, link, "$number")
                episode.p = this
                episode.anime = anime
                l.add(episode)
            }
        }

        return l
    }

    private fun nowDate(): String {
        return SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)
    }

    private fun toCalendar(iso8601string: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(iso8601string)
        calendar.time = date
        return calendar
    }
}