/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.google.gson.JsonObject
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.Impl.toHTTPS
import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.InputStreamReader
import java.net.URL
import java.util.*

@PlatformHandler(
    name = "Crunchyroll",
    url = "https://www.crunchyroll.com/",
    image = "images/platforms/crunchyroll.jpg",
    color = 0xFF6C00,
    countries = [FranceCountry::class]
)
class CrunchyrollPlatform : Platform() {
    data class Crunchyroll(val anime: String?, val image: String?, val description: String?)

    private val crunchyroll: MutableList<Crunchyroll> = emptyArray<Crunchyroll>().toMutableList()

    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val gson = Gson()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = Jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val inputStream =
                    URL("https://www.crunchyroll.com/rss/anime?lang=${country.checkOnEpisodesURL(this)}").openStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()

                Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.mapNotNull { Impl.toObject(it) }
                    ?.forEachIndexed { _, ejo ->
                        val releaseDate = ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(ejo, "pubDate")))
                            ?: return@forEachIndexed
                        if (!ISO8601.isSameDayUsingInstant(
                                calendar,
                                releaseDate
                            ) || calendar.before(releaseDate)
                        ) return@forEachIndexed
                        val ejoTitle = Impl.getString(ejo, "title") ?: return@forEachIndexed
                        val anime = Impl.getString(ejo, "seriesTitle") ?: return@forEachIndexed
                        val animeGenres =
                            Genre.getGenres(Impl.getArray(ejo, "keywords")?.mapNotNull { Impl.toString(it) }
                                ?.flatMap { it.split(", ") })
                        val season = Impl.getString(ejo, "season")?.toLongOrNull() ?: 1
                        val number = Impl.getString(ejo, "episodeNumber")?.toLongOrNull() ?: -1
                        val episodeType = if (number == -1L) EpisodeType.SPECIAL else EpisodeType.EPISODE
                        val langType = if (ejoTitle.contains(
                                "(${LangType.getData(countryImpl.country::class.java)?.data})",
                                true
                            )
                        ) LangType.VOICE else LangType.SUBTITLES
                        val episodeId = Impl.getString(ejo, "mediaId") ?: return@forEachIndexed
                        if (this.checkedEpisodes.contains(episodeId)) return@forEachIndexed
                        val title = Impl.getString(ejo, "episodeTitle")
                        val url = Impl.getString(ejo, "link")?.toHTTPS() ?: return@forEachIndexed
                        val image = Impl.getString(Impl.getArray(ejo, "thumbnail")?.mapNotNull { Impl.toObject(it) }
                            ?.maxByOrNull {
                                Impl.getString(it, "width")?.toLongOrNull()
                                    ?.times(Impl.getString(it, "height")?.toLongOrNull() ?: 0) ?: 0
                            }, "url")?.toHTTPS() ?: return@forEachIndexed
                        val duration = Impl.getString(ejo, "duration")?.toLongOrNull() ?: -1

                        if (!this.crunchyroll.any { it.anime.equals(anime, true) }) {
                            val animeId = url.split("/")[4]
                            val result =
                                JBrowser.get("${platformImpl.platformHandler.url}${country.restrictionEpisodes(this)}/$animeId")
                                    ?: return@forEachIndexed
                            val animeImage =
                                result.selectXpath("//*[@id=\"sidebar_elements\"]/li[1]/img").attr("src").toHTTPS()
                            var animeDescription = result.getElementsByClass("more").first()?.text()
                            if (animeDescription.isNullOrBlank()) animeDescription =
                                result.getElementsByClass("trunc-desc").text()
                            this.crunchyroll.add(Crunchyroll(anime, animeImage, animeDescription))
                        }

                        val crunchyroll =
                            this.crunchyroll.find { it.anime.equals(anime, true) } ?: return@forEachIndexed
                        val animeImage = crunchyroll.image ?: return@forEachIndexed
                        val animeDescription = crunchyroll.description

                        this.addCheckEpisodes(episodeId)
                        list.add(
                            Episode(
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
                                langType,
                                episodeId,
                                title,
                                url,
                                image,
                                duration
                            )
                        )
                }
            }
        }

        return list.toTypedArray()
    }
}