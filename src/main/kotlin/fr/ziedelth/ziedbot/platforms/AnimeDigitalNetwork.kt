package fr.ziedelth.ziedbot.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.ziedbot.utils.Request
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.EpisodeType
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

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()

    override fun getLastEpisodes(): Array<Episode> {
        val calendar = Calendar.getInstance()
        val l: MutableList<Episode> = mutableListOf()
        val response: String

        try {
            response = Request.get("https://gw.api.animedigitalnetwork.fr/video/calendar?date=${this.date(calendar)}")
        } catch (exception: Exception) {
            return l.toTypedArray()
        }

        val jsonObject = Gson().fromJson(response, JsonObject::class.java)
        val jsonArray = jsonObject.getAsJsonArray("videos")

        jsonArray.filter { it.isJsonObject }.forEach {
            val jObject = it.asJsonObject
            val showObject = jObject.getAsJsonObject("show")

            val releaseDate = this.toCalendar(jObject.get("releaseDate").asString)
            val season =
                if (jObject.has("season") && !jObject.get("season").isJsonNull) jObject.get("season").asString.toInt() else 1
            val anime = "${showObject.get("title").asString}${if (season > 1) " - Saison $season" else ""}"
            val title: String? =
                if (jObject.has("name") && !jObject.get("name").isJsonNull) jObject.get("name").asString else null
            val image = jObject.get("image2x").asString.replace(" ", "%20")
            val link = jObject.get("url").asString.replace(" ", "%20")
            val number = jObject.get("shortNumber").asString.toInt()
            val languages = jObject.get("languages").asJsonArray
            val language = if (languages.any { jsonElement ->
                    jsonElement.asString.equals(
                        "vf",
                        true
                    )
                }) EpisodeType.VOICE else EpisodeType.SUBTITLES
            val id = "${jObject.get("id").asInt}"

            if (calendar.after(releaseDate)) {
                val episode = Episode(
                    this.getName(),
                    toStringCalendar(releaseDate),
                    anime,
                    id,
                    title,
                    image,
                    link,
                    "$number",
                    language
                )
                episode.p = this
                l.add(episode)
            }
        }

        return l.toTypedArray()
    }

    private fun date(calendar: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
    }

    private fun toCalendar(iso8601string: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(iso8601string)
        calendar.time = date
        return calendar
    }
}