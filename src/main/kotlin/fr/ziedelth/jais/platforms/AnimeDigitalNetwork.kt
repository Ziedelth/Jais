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
    override fun getImage(): String = "https://ziedelth.fr/images/adn.png"
    override fun getColor(): Color = Color(0, 150, 255)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()

    override fun getLastEpisodes(): Array<Episode> {
        val calendar = Calendar.getInstance()
        val l: MutableList<Episode> = mutableListOf()

        this.getAllowedCountries().forEach { country ->
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

                val season: String = if (jObject.has("season") && !jObject["season"].isJsonNull) Const.toInt(
                    jObject["season"]?.asString,
                    "1"
                ) else "1"
                val anime =
                    if (showObject.has("originalTitle") && !showObject["originalTitle"].isJsonNull) showObject.get("originalTitle").asString else showObject.get(
                        "title"
                    ).asString

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
                val duration = jObject["duration"].asLong

                if (calendar.after(releaseDate)) {
                    val episode = Episode(
                        platform = this.getName(),
                        calendar = ISO8601.fromCalendar(releaseDate),
                        anime = anime,
                        season = season,
                        number = number,
                        country = country,
                        type = type,
                        id = id,
                        title = title,
                        image = image,
                        link = link,
                        duration = duration
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