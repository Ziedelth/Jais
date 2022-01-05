/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.Impl.toHTTPS
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
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
class AnimeDigitalNetworkPlatform(jais: Jais) : Platform(jais) {
    private fun getDate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val gson = Gson()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = this.jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val inputStream = URL(
                    "https://gw.api.animedigitalnetwork.${country.checkOnEpisodesURL(this)}/video/calendar?date=${
                        getDate(calendar)
                    }"
                ).openStream()
                val jsonObject: JsonObject? = gson.fromJson(InputStreamReader(inputStream), JsonObject::class.java)
                inputStream.close()

                Impl.getArray(jsonObject, "videos")?.mapNotNull { Impl.toObject(it) }?.forEachIndexed { _, ejo ->
                    val releaseDate = ISO8601.fromUTCDate(Impl.getString(ejo, "releaseDate")) ?: return@forEachIndexed
                    if (!ISO8601.isSameDayUsingInstant(
                            calendar,
                            releaseDate
                        ) || calendar.before(releaseDate)
                    ) return@forEachIndexed
                    val show = Impl.getObject(ejo, "show") ?: return@forEachIndexed
                    val anime =
                        Impl.getString(show, "shortTitle") ?: Impl.getString(show, "title") ?: return@forEachIndexed
                    val animeImage = Impl.getString(show, "image2x")?.toHTTPS()
                    val genresString = Impl.getArray(show, "genres")?.mapNotNull { Impl.toString(it) }
                    if (genresString?.contains("Animation japonaise") == false) return@forEachIndexed
                    val animeGenres = Genre.getGenres(genresString?.flatMap { it.split(" / ") })
                    val animeDescription = Impl.getString(show, "summary")
                    val season = Impl.getString(ejo, "season")?.toLongOrNull() ?: 1
                    val number = Impl.getString(ejo, "shortNumber")?.toLongOrNull() ?: -1
                    val episodeType = EpisodeType.EPISODE
                    val langType = LangType.getLangType(Impl.toString(Impl.getArray(ejo, "languages")?.lastOrNull()))
                    if (langType == LangType.UNKNOWN) return@forEachIndexed
                    val episodeId = Impl.getLong(ejo, "id")?.toString() ?: return@forEachIndexed

                    val title = Impl.getString(ejo, "name")
                    val url = Impl.getString(ejo, "url")?.toHTTPS() ?: return@forEachIndexed
                    val image = Impl.getString(ejo, "image2x")?.toHTTPS() ?: return@forEachIndexed
                    val duration = Impl.getLong(ejo, "duration") ?: -1

                    this.addEpisode(
                        title,
                        url,
                        image,
                        duration,
                        episodeId,
                        list,
                        platformImpl,
                        countryImpl,
                        releaseDate,
                        anime,
                        animeImage,
                        animeGenres,
                        animeDescription,
                        season,
                        number,
                        episodeType,
                        langType
                    )
                }
            }
        }

        return list.toTypedArray()
    }
}