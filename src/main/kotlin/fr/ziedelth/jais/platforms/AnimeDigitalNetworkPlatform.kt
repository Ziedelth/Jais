/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.platforms.AnimeDigitalNetworkEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

@PlatformHandler(
    name = "Anime Digital Network",
    url = "https://animedigitalnetwork.fr/",
    image = "https://ziedelth.fr/images/anime_digital_network.png",
    color = 0x0096FF,
    countries = [FranceCountry::class]
)
class AnimeDigitalNetworkPlatform : Platform() {
    override fun checkLastNews() {
        TODO("Not yet implemented")
    }

    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val list = mutableListOf<Episode>()
        val gson = Gson()

        this.getAllowedCountries().forEach { country ->
            try {
                val inputStream = URL(
                    "https://gw.api.animedigitalnetwork.${country.checkOnEpisodesURL(this)}/video/calendar?date=${
                        getDate(calendar)
                    }"
                ).openStream()
                val jsonObject: JsonObject? = gson.fromJson(InputStreamReader(inputStream), JsonObject::class.java)
                inputStream.close()
                var episodesList = (jsonObject?.get("videos") as JsonArray?)?.filter { it != null && it.isJsonObject }
                    ?.mapNotNull { gson.fromJson(it, AnimeDigitalNetworkEpisode::class.java) }
                episodesList?.forEach { it.platform = this; it.country = country }
                episodesList = episodesList?.filter {
                    it.isValid() && ISO8601.isSameDayUsingInstant(
                        calendar,
                        ISO8601.toCalendar1(it.releaseDate)
                    ) && calendar.after(ISO8601.toCalendar1(it.releaseDate))
                }?.sortedBy { ISO8601.toCalendar1(it.releaseDate) }

                episodesList?.mapNotNull { it.toEpisode() }?.let { list.addAll(it) }
            } catch (exception: Exception) {
                JLogger.log(
                    Level.SEVERE,
                    "Failed to get ${this.javaClass.simpleName} episode(s) : ${exception.message}",
                    exception
                )
            }
        }

        return list.toTypedArray()
    }

    private fun getDate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}