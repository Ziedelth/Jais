/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Request
import fr.ziedelth.jais.utils.animes.*
import org.jsoup.Jsoup
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.Color
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

class AnimeDigitalNetwork : Platform {
    private val calendars: MutableMap<Country, MutableList<String>> = mutableMapOf()
    private var lastDate: String? = null

    override fun getName(): String = "Anime Digital Network"
    override fun getURL(): String = "https://animedigitalnetwork.fr/"
    override fun getImage(): String = "https://ziedelth.fr/images/adn.png"
    override fun getColor(): Color = Color(0, 150, 255)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    private fun getVideos(country: Country, calendar: Calendar): JsonArray {
        val response: String

        try {
            response = Request.get(
                "https://gw.api.animedigitalnetwork.${country.country}/video/calendar?date=${
                    getDate(calendar)
                }"
            )
        } catch (exception: Exception) {
            return JsonArray()
        }

        val jsonObject = Gson().fromJson(response, JsonObject::class.java)
        return jsonObject.getAsJsonArray("videos")
    }

    override fun getLastNews(): Array<News> {
        val calendar = Calendar.getInstance()
        val l: MutableList<News> = mutableListOf()

        this.getAllowedCountries().forEach { country ->
            val url: URLConnection
            val list: NodeList

            try {
                url =
                    URL("https://www.animenewsnetwork.com/all/rss.xml?ann-edition=${country.country}").openConnection()
                list = Const.getItems(url, "item")
            } catch (exception: Exception) {
                return l.toTypedArray()
            }

            for (i in 0 until list.length) {
                val node = list.item(i)

                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element

                    val date = element.getElementsByTagName("pubDate").item(0).textContent
                    val releaseDate = toCalendar(date)
                    if (!(Const.isSameDay(calendar, releaseDate) && calendar.after(releaseDate))) continue

                    val title = element.getElementsByTagName("title").item(0).textContent
                    val description =
                        Jsoup.parse(element.getElementsByTagName("description").item(0).textContent).text()
                    val link = element.getElementsByTagName("guid").item(0).textContent
                    val category = element.getElementsByTagName("category").item(0).textContent
                    if (!category.equals("Anime", true)) continue

                    if (Const.isSameDay(calendar, releaseDate)) {
                        val news =
                            News(
                                this.getName(),
                                ISO8601.fromCalendar(releaseDate),
                                title,
                                description,
                                "",
                                link,
                                country
                            )
                        news.p = this
                        l.add(news)
                    }
                }
            }
        }

        return l.toTypedArray()
    }

    override fun getLastEpisodes(): Array<Episode> {
        val calendar = Calendar.getInstance()
        val date = getDate(calendar)
        val l: MutableList<Episode> = mutableListOf()

        if (this.lastDate != date) {
            this.calendars.clear()

            this.getAllowedCountries().forEach { country ->
                this.calendars[country] = getVideos(country, calendar).filter { it.isJsonObject }.map {
                    val jObject = it.asJsonObject
                    ISO8601.fromCalendar(ISO8601.toCalendar(jObject.get("releaseDate").asString))
                }.stream().distinct().collect(Collectors.toList()).toMutableList()
            }

            this.lastDate = date
        }

        this.getAllowedCountries().filter { country ->
            val fc = this.calendars.getOrDefault(country, mutableListOf()).firstOrNull()
            !fc.isNullOrEmpty() && calendar.after(ISO8601.toCalendar(fc))
        }.forEach { country ->
            val f = this.calendars.getOrDefault(country, mutableListOf())
            f.removeFirst()

            getVideos(country, calendar).filter { it.isJsonObject }.forEachIndexed { _, jsonVideoElement ->
                val jObject = jsonVideoElement.asJsonObject
                val showObject = jObject.getAsJsonObject("show")
                val releaseDate = ISO8601.toCalendar(jObject.get("releaseDate").asString)
                if (calendar.before(releaseDate)) return@forEachIndexed

                val season = if (jObject.has("season") && !jObject["season"].isJsonNull) Const.toInt(
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
                val id = jObject.get("id").asLong
                val duration = jObject["duration"].asLong

                if (calendar.after(releaseDate)) {
                    l.add(
                        Episode(
                            platform = this,
                            calendar = ISO8601.fromCalendar(releaseDate),
                            anime = anime,
                            number = number,
                            country = country,
                            type = type,
                            season = season,
                            episodeId = id,
                            title = title,
                            image = image,
                            url = link,
                            duration = duration
                        )
                    )
                }
            }
        }

        return l.toTypedArray()
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH).parse(s)
        calendar.time = date
        return calendar
    }
}