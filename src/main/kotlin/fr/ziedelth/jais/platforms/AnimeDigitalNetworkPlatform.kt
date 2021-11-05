/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.platforms.AnimeDigitalNetworkEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@PlatformHandler(
    name = "Anime Digital Network",
    url = "https://animedigitalnetwork.fr/",
    image = "images/platforms/anime_digital_network.jpg",
    color = 0x0096FF,
    countries = [FranceCountry::class]
)
class AnimeDigitalNetworkPlatform : Platform() {
    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val list = mutableListOf<Episode>()
        val gson = Gson()

        this.getAllowedCountries().forEach { country ->
            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val inputStream = URL(
                    "https://gw.api.animedigitalnetwork.${country.checkOnEpisodesURL(this)}/video/calendar?date=${
                        getDate(calendar)
                    }"
                ).openStream()
                val jsonObject: JsonObject? = gson.fromJson(InputStreamReader(inputStream), JsonObject::class.java)
                inputStream.close()
                val episodesList = (jsonObject?.get("videos") as JsonArray?)?.filter { it != null && it.isJsonObject }
                    ?.mapNotNull { gson.fromJson(it, AnimeDigitalNetworkEpisode::class.java) }
                episodesList?.forEach { it.platform = this; it.country = country }

                episodesList?.filter {
                    !this.checkedEpisodes.contains(it.id.toString()) && it.isValid() && ISO8601.isSameDayUsingISO8601(
                        ISO8601.fromCalendar1(it.releaseDate),
                        ISO8601.fromCalendar(calendar)
                    ) && calendar.after(ISO8601.toCalendar1(it.releaseDate)) && it.show?.genres?.map { g -> g.lowercase() }
                        ?.contains("Animation japonaise".lowercase()) == true
                }?.sortedBy { ISO8601.toCalendar1(it.releaseDate) }?.forEachIndexed { _, animeDigitalNetworkEpisode ->
                    val episode = animeDigitalNetworkEpisode.toEpisode() ?: return@forEachIndexed
                    list.add(episode)
                    this.addCheckEpisodes(animeDigitalNetworkEpisode.id!!.toString())
                }
            }
        }

        return list.toTypedArray()
    }

    private fun getDate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}