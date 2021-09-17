/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.episodes.CrunchyrollEpisode
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.InputStreamReader
import java.net.URL
import java.util.*

@PlatformHandler(
    name = "Crunchyroll",
    url = "https://www.crunchyroll.com/",
    image = "https://ziedelth.fr/images/crunchyroll.png",
    color = 0x0096FF,
    countries = [FranceCountry::class]
)
class CrunchyrollPlatform : Platform() {
    override fun checkLastNews() {
        TODO("Not yet implemented")
    }

    override fun checkLastEpisodes(): Array<Episode> {
        val list = mutableListOf<Episode>()
        val calendar = Calendar.getInstance()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        JLogger.info("Fetch ${this.javaClass.simpleName} episode(s)")
        val start = System.currentTimeMillis()

        this.getAllowedCountries().forEach { country ->
            try {
                val inputStream =
                    URL("https://www.crunchyroll.com/rss/anime?lang=${country.checkOnEpisodesURL(this)}").openStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()
                var episodesList =
                    (jsonObject?.get("channel") as JsonObject?)?.get("item")?.asJsonArray?.filter { it != null && it.isJsonObject }
                        ?.mapNotNull { gson.fromJson(it, CrunchyrollEpisode::class.java) }
                episodesList?.forEach { it.platform = this; it.country = country }
                episodesList = episodesList?.filter {
                    it.isValid() && ISO8601.isSameDayUsingInstant(
                        calendar,
                        ISO8601.toCalendar2(it.pubDate)
                    ) && calendar.after(ISO8601.toCalendar2(it.pubDate))
                }?.sortedBy { ISO8601.toCalendar2(it.pubDate) }

                JLogger.config("${episodesList?.size ?: 0}")
                JLogger.config(episodesList?.mapNotNull { ISO8601.fromCalendar(ISO8601.toCalendar2(it.pubDate)) }
                    ?.distinct()?.toTypedArray()?.contentToString())
                JLogger.config("$episodesList")
                JLogger.config("Fetch in ${System.currentTimeMillis() - start}ms")

                episodesList?.mapNotNull { it.toEpisode() }?.let { list.addAll(it) }
            } catch (exception: Exception) {
                JLogger.severe("Failed to get ${this.javaClass.simpleName} episode(s) : ${exception.message}")
            }
        }

        return list.toTypedArray()
    }
}