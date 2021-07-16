package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Request
import fr.ziedelth.jais.utils.animes.*
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

                val season: String? = if (!jsonObject.has("season") || jObject["season"]?.isJsonNull == true) {
                    null
                } else {
                    try {
                        val i: Int = jObject["season"].asString.toInt()
                        if (i > 1) "Saison $i"
                        else null
                    } catch (exception: Exception) {
                        jObject["season"].asString
                    }
                }

                var anime = showObject.get("title").asString
                if (!anime.contains("Saison", true) && season != null) anime += " - $season"

                val title: String? =
                    if (jObject.has("name") && !jObject.get("name").isJsonNull) jObject.get("name").asString else null
                val image = jObject.get("image2x").asString.replace(" ", "%20")
                val link = jObject.get("url").asString.replace(" ", "%20")
                val number = Const.toInt(jObject.get("shortNumber").asString)
                val languages = jObject.get("languages").asJsonArray
                val type = if (languages.any { jsonElement ->
                        jsonElement.asString.equals(
                            country.dubbed,
                            true
                        )
                    }) EpisodeType.DUBBED else EpisodeType.SUBTITLED
                val id = "${jObject.get("id").asInt}"

                if (calendar.after(releaseDate)) {
                    val episode = Episode(
                        platform = this.getName(),
                        calendar = ISO8601.fromCalendar(releaseDate),
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