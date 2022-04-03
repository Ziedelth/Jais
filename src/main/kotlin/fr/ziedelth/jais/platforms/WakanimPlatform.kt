/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
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
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

@PlatformHandler(
    name = "Wakanim",
    url = "https://wakanim.tv/",
    image = "images/platforms/wakanim.jpg",
    color = 0xE3474B,
    countries = [FranceCountry::class]
)
class WakanimPlatform(jais: Jais) : Platform(jais) {
    data class Wakanim(val anime: String?, val image: String?, val smallSummary: String?, val genres: Array<Genre>?) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Wakanim

            if (anime != other.anime) return false
            if (image != other.image) return false
            if (smallSummary != other.smallSummary) return false
            if (genres != null) {
                if (other.genres == null) return false
                if (!genres.contentEquals(other.genres)) return false
            } else if (other.genres != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = anime?.hashCode() ?: 0
            result = 31 * result + (image?.hashCode() ?: 0)
            result = 31 * result + (smallSummary?.hashCode() ?: 0)
            result = 31 * result + (genres?.contentHashCode() ?: 0)
            return result
        }
    }

    private val wakanim: MutableList<Wakanim> = mutableListOf()
    private val lastCheck = mutableMapOf<Country, Long>()
    private val cElements = mutableMapOf<Country, Elements?>()

    /**
     * It checks the episodes for the current day and returns them
     *
     * @param calendar The calendar object that contains the date of the episode.
     * @return An array of Episode objects.
     */
    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val date = getDate(calendar)
        val gson = Gson()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = this.jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                if (System.currentTimeMillis() - (this.lastCheck[country]
                        ?: 0) >= 3600000 || (!this.cElements.containsKey(country) || this.cElements[country] == null)
                ) {
                    this.lastCheck[country] = System.currentTimeMillis()

                    val url =
                        "https://www.wakanim.tv/${country.checkOnEpisodesURL(this)}/v2/agenda/getevents?s=$date&e=$date&free=false".toHTTPS()
                    val result = JBrowser.get(url)
                    this.cElements[country] = result?.getElementsByClass("Calendar-ep")

                    val inputStream = URL("https://account.wakanim.tv/api/catalogue").openStream()
                    val jsonArray: JsonArray? = gson.fromJson(InputStreamReader(inputStream), JsonArray::class.java)
                    inputStream.close()

                    this.wakanim.clear()

                    this.wakanim.addAll(jsonArray?.filter { it != null && it.isJsonObject }?.mapNotNull { jsonElement ->
                        val jsonObject: JsonObject? = gson.fromJson(jsonElement, JsonObject::class.java)
                        Wakanim(
                            jsonObject?.get("name")?.asString?.dropLastWhile(Char::isWhitespace),
                            jsonObject?.get("imageUrl")?.asString?.toHTTPS(),
                            jsonObject?.get("smallSummary")?.asString,
                            jsonObject?.get("genres")?.asJsonArray?.filter { it != null && it.isJsonObject }
                                ?.mapNotNull {
                                    Genre.getGenre(
                                        it.asJsonObject.get("name")?.asString?.dropLastWhile(Char::isWhitespace) ?: ""
                                    )
                                }?.filter { it != Genre.UNKNOWN }?.toTypedArray()
                        )
                    }?.toTypedArray() ?: emptyArray())
                }

                this.cElements[country]?.forEachIndexed { index, it ->
                    val text = it?.text()
                    val ts = text?.split(" ")
                    val releaseDate =
                        ISO8601.fromUTCDate("${this.getISODate(calendar)}T${ts?.get(0)}:00Z") ?: return@forEachIndexed
                    if (!ISO8601.isSameDayUsingInstant(
                            calendar,
                            releaseDate
                        ) || calendar.before(releaseDate)
                    ) return@forEachIndexed
                    val anime = ts?.subList(1, ts.indexOf("Séries"))?.joinToString(" ") ?: return@forEachIndexed
                    val number = ts[ts.size - 2].replace(" ", "").toLongOrNull()

                    val etc = ts.subList(ts.indexOf("Séries") + 1, ts.size - 2).joinToString(" ")

                    var episodeType =
                        if (etc.contains("${EpisodeType.FILM.getData(countryImpl.country.javaClass)?.data}", true))
                            EpisodeType.FILM
                        else if (etc.contains(
                                "${EpisodeType.SPECIAL.getData(countryImpl.country.javaClass)?.data}",
                                true
                            )
                        )
                            EpisodeType.SPECIAL
                        else
                            EpisodeType.EPISODE

                    val langType = LangType.getLangType(ts[ts.size - 1].replace(" ", ""))
                    if (langType == LangType.UNKNOWN) return@forEachIndexed
                    val checkUrl = "https://www.wakanim.tv${
                        it.getElementsByClass("Calendar-linkImg").firstOrNull()?.attr("href")
                    }".toHTTPS()
                    val wakanimType = checkUrl.split("/")[6]

                    val hash = Base64.getEncoder()
                        .encodeToString("$index${anime.onlyLettersAndDigits()}$number$langType".encodeToByteArray())
                    if (hash.isBlank() || this.checkedEpisodes.contains(hash)) return@forEachIndexed

                    val episodeResult = JBrowser.get(checkUrl)

                    val cardEpisodeElement = if (wakanimType.equals("episode", true)) {
                        episodeResult?.getElementsByClass("currentEp")?.firstOrNull {
                            it.getElementsByClass("slider_item_number").text().toLongOrNull() == number
                        }
                    } else {
                        if (!hasEpisodes(episodeResult)) return@forEachIndexed
                        episodeResult?.getElementsByClass("slider_item")?.firstOrNull {
                            it.hasClass("-big") && it.getElementsByClass("slider_item_number").text()
                                .toLongOrNull() == number
                        }
                    }

                    val cardNumber =
                        cardEpisodeElement?.getElementsByClass("slider_item_number")?.text()?.toLongOrNull()

                    if (number != null && cardNumber != null && number == cardNumber) {
                        val url = "https://www.wakanim.tv${
                            cardEpisodeElement.getElementsByClass("slider_item_star").attr("href")
                        }".toHTTPS()
                        val episodeId = url.split("/")[7]
                        if (episodeId.isBlank() || this.checkedEpisodes.contains(episodeId)) return@forEachIndexed
                        val image = "https:${cardEpisodeElement.getElementsByTag("img").attr("src")}".toHTTPS()
                        val cardSeason = cardEpisodeElement.getElementsByClass("slider_item_season").text()

                        var season = 1L

                        if (cardSeason.contains(countryImpl.countryHandler.season, true)) {
                            val split = cardSeason.split(" ")
                            season = split[split.indexOf((countryImpl.countryHandler.season)) + 1].toLongOrNull() ?: 1L
                        }
                        // If contains OVA in title of season, it's special episode
                        else if (cardSeason.contains("OVA", true)) {
                            episodeType = EpisodeType.SPECIAL
                        }
                        // If contains film in title of season, it's a film
                        else if (cardSeason.contains(
                                "${EpisodeType.FILM.getData(countryImpl.country.javaClass)?.data}",
                                true
                            )
                        ) {
                            episodeType = EpisodeType.FILM
                        }

                        val cardDuration =
                            cardEpisodeElement.getElementsByClass("slider_item_duration").text().split(":")
                        var duration = cardDuration.mapIndexed { i, t ->
                            (t.ifEmpty { "0" }.toLongOrNull()
                                ?.times(60.0.pow(((cardDuration.size - i) - 1).toDouble())) ?: 0L).toLong()
                        }.sum()
                        if (duration <= 0) duration = -1

                        val wakanim = this.wakanim.firstOrNull { it.anime.equals(anime, true) } ?: return@forEachIndexed
                        val animeImage = wakanim.image
                        val animeGenres = wakanim.genres ?: emptyArray()
                        val animeDescription = wakanim.smallSummary

                        this.addCheck(hash)
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
                                null,
                                url,
                                image,
                                duration
                            )
                        )
                    }
                }
            }
        }

        return list.toTypedArray()
    }

    /**
     * If the document has a class named "NoEpisodes", then return false. Otherwise, return true
     *
     * @param episodeResult Document?
     * @return Nothing.
     */
    private fun hasEpisodes(episodeResult: Document?): Boolean {
        try {
            if (episodeResult?.getElementsByClass("NoEpisodes")?.firstOrNull() != null) return false
        } catch (_: Exception) {
        }

        return true
    }

    /**
     * Get the date from the calendar and format it as a string
     *
     * @param calendar The Calendar object that you want to convert to a date.
     */
    private fun getDate(calendar: Calendar): String = SimpleDateFormat("dd-MM-yyyy").format(calendar.time)

    /**
     * Get the date in ISO format
     *
     * @param calendar The Calendar object to be used to generate the date.
     */
    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}