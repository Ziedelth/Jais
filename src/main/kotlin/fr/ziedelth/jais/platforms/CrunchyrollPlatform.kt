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
import fr.ziedelth.jais.utils.animes.*
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import org.jsoup.Jsoup
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
class CrunchyrollPlatform(jais: Jais) : Platform(jais) {
    data class Crunchyroll(val anime: String?, val image: String?, val description: String?)

    private val crunchyroll: MutableList<Crunchyroll> = mutableListOf()

    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val pairPlatformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val gson = Gson()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { pairCountryImpl ->
            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val urlConnection =
                    URL("https://www.crunchyroll.com/rss/anime?lang=${pairCountryImpl.second.checkOnEpisodesURL(this)}").openConnection()
                urlConnection.connectTimeout = 10000
                urlConnection.readTimeout = 10000
                val inputStream = urlConnection.getInputStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()

                Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.mapNotNull { Impl.toObject(it) }
                    ?.forEachIndexed { _, ejo ->
                        val restriction = Impl.getString(Impl.getObject(ejo, "restriction"), "")?.split(" ")
                        if (restriction?.contains(pairCountryImpl.second.restrictionEpisodes(this)) != true) return@forEachIndexed
                        val ejoTitle = Impl.getString(ejo, "title") ?: return@forEachIndexed
                        var langType = LangType.SUBTITLES

                        for (d in LangType.VOICE.getDatas(pairCountryImpl.second::class.java)) {
                            if (ejoTitle.contains("(${d.data})", true)) {
                                langType = LangType.VOICE
                                break
                            }
                        }

                        val subtitles = Impl.getString(ejo, "subtitleLanguages")?.split(",")
                        if (langType == LangType.SUBTITLES && subtitles?.contains(
                                pairCountryImpl.second.subtitlesEpisodes(
                                    this
                                )
                            ) != true
                        ) return@forEachIndexed
                        val releaseDate = ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(ejo, "pubDate")))
                            ?: return@forEachIndexed
                        if (!ISO8601.isSameDayUsingInstant(
                                calendar,
                                releaseDate
                            ) || calendar.before(releaseDate)
                        ) return@forEachIndexed
                        val anime = Impl.getString(ejo, "seriesTitle") ?: return@forEachIndexed
                        val animeGenres = Genre.getGenres(Impl.getString(ejo, "keywords")?.split(", "))
                        val season = Impl.getString(ejo, "season")?.toLongOrNull() ?: 1
                        val number = Impl.getString(ejo, "episodeNumber")?.toLongOrNull() ?: -1
                        val episodeType = if (number == -1L) EpisodeType.SPECIAL else EpisodeType.EPISODE
                        val episodeId = Impl.getString(ejo, "mediaId") ?: return@forEachIndexed

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
                                JBrowser.get(
                                    "${pairPlatformImpl.first.url}${
                                        pairCountryImpl.second.restrictionEpisodes(
                                            this
                                        )
                                    }/$animeId"
                                )
                            val animeImage =
                                result?.selectXpath("//*[@id=\"sidebar_elements\"]/li[1]/img")?.attr("src")?.toHTTPS()
                            var animeDescription = result?.getElementsByClass("more")?.first()?.text()
                            if (animeDescription.isNullOrBlank()) animeDescription =
                                result?.getElementsByClass("trunc-desc")?.text()
                            this.crunchyroll.add(Crunchyroll(anime, animeImage, animeDescription))
                        }

                        val crunchyroll =
                            this.crunchyroll.find { it.anime.equals(anime, true) } ?: return@forEachIndexed
                        val animeImage = crunchyroll.image
                        val animeDescription = crunchyroll.description

                        this.addEpisode(
                            title,
                            url,
                            image,
                            duration,
                            episodeId,
                            list,
                            pairPlatformImpl,
                            pairCountryImpl,
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

    @Synchronized
    override fun checkNews(calendar: Calendar): Array<News> {
        val pairPlatformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<News>()
        val gson = Gson()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { pairCountryImpl ->
            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} news:") {
                val urlConnection =
                    URL("https://www.crunchyroll.com/newsrss?lang=${pairCountryImpl.second.checkOnEpisodesURL(this)}").openConnection()
                urlConnection.connectTimeout = 10000
                urlConnection.readTimeout = 10000
                val inputStream = urlConnection.getInputStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()

                Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.mapNotNull { Impl.toObject(it) }
                    ?.forEachIndexed { _, njo ->
                        val title = Impl.getString(njo, "title") ?: return@forEachIndexed
                        if (this.checkedEpisodes.contains(title)) return@forEachIndexed
                        val description =
                            Jsoup.parse(Impl.getString(njo, "description") ?: "").text() ?: return@forEachIndexed
                        val url = Impl.getString(njo, "guid") ?: return@forEachIndexed
                        val releaseDate = ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(njo, "pubDate")))
                            ?: return@forEachIndexed

                        if (!ISO8601.isSameDayUsingInstant(
                                calendar,
                                releaseDate
                            ) || calendar.before(releaseDate)
                        ) return@forEachIndexed

                        this.addCheck(title)
                        list.add(News(pairPlatformImpl, pairCountryImpl, releaseDate, title, description, url))
                    }
            }
        }

        return list.toTypedArray()
    }
}