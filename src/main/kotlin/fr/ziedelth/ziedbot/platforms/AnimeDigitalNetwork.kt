package fr.ziedelth.ziedbot.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.ziedbot.utils.ISO8601
import fr.ziedelth.ziedbot.utils.Request
import fr.ziedelth.ziedbot.utils.animes.*
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class AnimeDigitalNetwork : Platform {
    override fun getName(): String = "Anime Digital Network"
    override fun getURL(): String = "https://animedigitalnetwork.fr/"
    override fun getImage(): String = "https://img.apksum.com/e6/fr.anidn/v4.1.65/icon.png"
    override fun getColor(): Color = Color(0, 150, 255)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()

    override fun getLastEpisodes(): Array<Episode> {
        val calendar = Calendar.getInstance()
        val l: MutableList<Episode> = mutableListOf()

        Country.values().filter { this.getAllowedCountries().contains(it) }.forEach { country ->
            val response: String

            try {
                response = Request.get(
                    "https://gw.api.animedigitalnetwork.${country.country}/video/calendar?date=${
                        this.date(calendar)
                    }"
                )
            } catch (exception: Exception) {
                return l.toTypedArray()
            }

            val jsonObject = Gson().fromJson(response, JsonObject::class.java)
            val jsonArray = jsonObject.getAsJsonArray("videos")

            jsonArray.filter { it.isJsonObject }.forEach {
                val jObject = it.asJsonObject
                val showObject = jObject.getAsJsonObject("show")

                val releaseDate = ISO8601.toCalendar(jObject.get("releaseDate").asString)

                val a: Boolean = try {
                    if (jObject.has("season") && !jObject.get("season").isJsonNull) jObject.get("season").asString.toInt()
                    true
                } catch (exception: Exception) {
                    false
                }
                val season: String? = if (jObject.has("season") && !jObject.get("season").isJsonNull) {
                    if (a) "${jObject.get("season").asString.toInt()}" else jObject.get("season").asString
                } else null

                val anime = "${showObject.get("title").asString}${
                    if (season != null) {
                        if (a) {
                            if (season.toInt() <= 1) "" else " - Saison $season"
                        } else " - $season"
                    } else ""
                }"
                val title: String? =
                    if (jObject.has("name") && !jObject.get("name").isJsonNull) jObject.get("name").asString else null
                val image = jObject.get("image2x").asString.replace(" ", "%20")
                val link = jObject.get("url").asString.replace(" ", "%20")
                val number = jObject.get("shortNumber").asString
                val languages = jObject.get("languages").asJsonArray
                val type = if (languages.any { jsonElement ->
                        jsonElement.asString.equals(
                            country.voice,
                            true
                        )
                    }) EpisodeType.VOICE else EpisodeType.SUBTITLES
                val id = "${jObject.get("id").asInt}"

                if (calendar.after(releaseDate)) {
                    val episode = Episode(
                        platform = this.getName(),
                        calendar = toStringCalendar(releaseDate),
                        anime = anime,
                        id = id,
                        title = title,
                        image = image,
                        link = link,
                        number = number,
                        country = country,
                        type = type
                    )
                    episode.p = this
                    l.add(episode)
                }
            }
        }

        return l.toTypedArray()
    }

    private fun date(calendar: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
    }
}