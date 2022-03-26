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
import fr.ziedelth.jais.utils.animes.*
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import org.jsoup.Jsoup
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
    /**
     * Get the date from the calendar and format it as a string
     *
     * @param calendar The Calendar object that you want to convert to a string.
     */
    private fun getDate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

    /**
     * It gets the episodes for the current day and adds them to the list
     *
     * @param calendar The calendar object that contains the date to check for episodes.
     * @return An array of Episode objects.
     */
    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val gson = Gson()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = this.jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val urlConnection = URL(
                    "https://gw.api.animedigitalnetwork.${country.checkOnEpisodesURL(this)}/video/calendar?date=${
                        getDate(calendar)
                    }"
                ).openConnection()
                urlConnection.connectTimeout = 10000
                urlConnection.readTimeout = 10000
                val inputStream = urlConnection.getInputStream()
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

    /**
     * It checks the RSS feed for the latest news and adds them to the list if they are not already in the list
     *
     * @param calendar The calendar to check against.
     * @return An array of News objects.
     */
    @Synchronized
    override fun checkNews(calendar: Calendar): Array<News> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<News>()
        val gson = Gson()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = this.jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} news:") {
                val urlConnection =
                    URL("https://www.animenewsnetwork.com/all/rss.xml?ann-edition=${country.checkOnEpisodesURL(this)}").openConnection()
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
                        val category = Impl.getString(njo, "category")
                        if (!(category.equals("Anime", true) || category.equals("Manga", true))) return@forEachIndexed
                        val title = Impl.getString(njo, "title") ?: return@forEachIndexed
                        if (this.checkedEpisodes.contains(title)) return@forEachIndexed
                        val description =
                            Jsoup.parse(Impl.getString(njo, "description") ?: "").text() ?: return@forEachIndexed
                        val url = Impl.getString(njo, "link") ?: return@forEachIndexed
                        val releaseDate = ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(njo, "pubDate")))
                            ?: return@forEachIndexed

                        if (!ISO8601.isSameDayUsingInstant(
                                calendar,
                                releaseDate
                            ) || calendar.before(releaseDate)
                        ) return@forEachIndexed

                        this.addCheck(title)
                        list.add(News(platformImpl, countryImpl, releaseDate, title, description, url))
                    }
            }
        }

        return list.toTypedArray()
    }
}