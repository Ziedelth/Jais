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
import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
import fr.ziedelth.jais.utils.animes.episodes.platforms.WakanimEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
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
class WakanimPlatform : Platform() {
    data class WakanimAnime(
        val anime: String?,
        val image: String?,
        val smallSummary: String?,
        val genres: Array<AnimeGenre>?
    )

    private val animes: Array<WakanimAnime>
    private val lastCheck = mutableMapOf<Country, Long>()

    init {
        val gson = Gson()
        val inputStream = URL("https://account.wakanim.tv/api/catalogue").openStream()
        val jsonArray: JsonArray? = gson.fromJson(InputStreamReader(inputStream), JsonArray::class.java)
        inputStream.close()

        this.animes = jsonArray?.filter { it != null && it.isJsonObject }?.mapNotNull { jsonElement ->
            val jsonObject: JsonObject? = gson.fromJson(jsonElement, JsonObject::class.java)
            WakanimAnime(
                jsonObject?.get("name")?.asString?.dropLastWhile(Char::isWhitespace),
                jsonObject?.get("imageUrl")?.asString,
                jsonObject?.get("smallSummary")?.asString,
                jsonObject?.get("genres")?.asJsonArray?.filter { it != null && it.isJsonObject }?.mapNotNull {
                    AnimeGenre.getGenre(
                        it.asJsonObject.get("name")?.asString?.dropLastWhile(Char::isWhitespace) ?: ""
                    )
                }?.filter { it != AnimeGenre.UNKNOWN }?.toTypedArray()
            )
        }?.toTypedArray() ?: emptyArray()
    }

    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val list = mutableListOf<Episode>()
        val date = getDate(calendar)

        this.getAllowedCountries().forEach { country ->
            val countryInformation = Jais.getCountryInformation(country)

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val episodesList = mutableListOf<WakanimEpisode>()

                if (System.currentTimeMillis() - (this.lastCheck[country] ?: 0) >= 3600000) {
                    this.lastCheck[country] = System.currentTimeMillis()

                    val url =
                        "https://www.wakanim.tv/${country.checkOnEpisodesURL(this)}/v2/agenda/getevents?s=$date&e=$date&free=false"
                    val result = JBrowser.get(url)

                    val elements = result?.getElementsByClass("Calendar-ep")

                    elements?.forEachIndexed { _, it ->
                        val text = it?.text()
                        val ts = text?.split(" ")
                        val time = ISO8601.fromCalendar1("${this.getISODate(calendar)}T${ts?.get(0)}:00Z")
                        val anime = ts?.subList(1, ts.size - 5)?.joinToString(" ")
                        val number = ts?.get(ts.size - 2)?.replace(" ", "")?.toLongOrNull()
                        val episodeType =
                            if (EpisodeType.FILM.getData(countryInformation?.country?.javaClass)?.data == ts?.get(ts.size - 4)) EpisodeType.FILM else EpisodeType.EPISODE
                        if (episodeType == EpisodeType.UNKNOWN) return@forEachIndexed
                        val langType = LangType.getLangType(ts?.get(ts.size - 1)?.replace(" ", ""))
                        if (langType == LangType.UNKNOWN) return@forEachIndexed
                        val checkUrl = "https://www.wakanim.tv${
                            it.getElementsByClass("Calendar-linkImg").firstOrNull()?.attr("href")
                        }"
                        val wakanimType = checkUrl.split("/")[6]

                        if (wakanimType.equals("episode", true)) {
                            val hash = Base64.getEncoder()
                                .encodeToString("$time$anime$number$episodeType$langType".encodeToByteArray())
                            val episodeId = checkUrl.split("/")[7]
                            if (this.checkedEpisodes.contains(hash) || this.checkedEpisodes.contains(episodeId)) return@forEachIndexed
                        } else {
                            val episodeId = Base64.getEncoder()
                                .encodeToString("$time$anime$number$episodeType$langType".encodeToByteArray())
                            if (episodeId == null || this.checkedEpisodes.contains(episodeId)) return@forEachIndexed
                        }

                        val episodeResult = JBrowser.get(checkUrl)

                        val cardEpisodeElement = if (wakanimType.equals("episode", true)) {
                            episodeResult?.getElementsByClass("currentEp")?.firstOrNull()
                        } else {
                            try {
                                if (episodeResult?.getElementsByClass("NoEpisodes")
                                        ?.firstOrNull() != null
                                ) return@forEachIndexed
                            } catch (exception: Exception) {
                            }

                            episodeResult?.getElementsByClass("slider_item")?.lastOrNull()
                        }

                        val cardNumber =
                            cardEpisodeElement?.getElementsByClass("slider_item_number")?.text()?.toLongOrNull()

                        if (number != null && cardNumber != null && number == cardNumber) {
                            val cardUrl = "https://www.wakanim.tv${
                                cardEpisodeElement.getElementsByClass("slider_item_star").attr("href")
                            }"
                            val episodeId = cardUrl.split("/")[7]
                            if (episodeId.isBlank() || this.checkedEpisodes.contains(episodeId)) return@forEachIndexed
                            val image = "https:${cardEpisodeElement.getElementsByTag("img").attr("src")}"
                            val cardSeason = cardEpisodeElement.getElementsByClass("slider_item_season").text()

                            val season = if (cardSeason.contains(
                                    Jais.getCountryInformation(country)!!.countryHandler.season,
                                    true
                                )
                            ) {
                                val split = cardSeason.split(" ")
                                split[split.indexOf((Jais.getCountryInformation(country)!!.countryHandler.season)) + 1].toLongOrNull()
                            } else 1L

                            val cardDuration =
                                cardEpisodeElement.getElementsByClass("slider_item_duration").text().split(":")
                            val duration = cardDuration.mapIndexed { index, t ->
                                (t.ifEmpty { "0" }.toLongOrNull()
                                    ?.times(60.0.pow(((cardDuration.size - index) - 1).toDouble())) ?: 0L).toLong()
                            }.sum()

                            val wakanimAnime = this.animes.firstOrNull { it.anime.equals(anime, true) }

                            episodesList.add(
                                WakanimEpisode(
                                    releaseDate = time,
                                    anime = anime,
                                    animeImage = wakanimAnime?.image,
                                    animeGenres = wakanimAnime?.genres ?: emptyArray(),
                                    animeDescription = wakanimAnime?.smallSummary,
                                    season = season,
                                    number = number,
                                    episodeType = episodeType,
                                    langType = langType,
                                    episodeId = episodeId.toLongOrNull(),
                                    image = image,
                                    duration = duration,
                                    url = cardUrl
                                )
                            )
                        }
                    }

//                    for (i in 0 until (this.bodyText[country]?.indexOfFirst { it.equals("Aujourd'hui", true) || it[max(0, it.length - 3)] == '/' } ?: 0) step 4) {
//                        val episodeText = bodyText?.subList(i, i + 4)?.joinToString()?.split(",")
//                        println(episodeText)
//                    }

//                    JLogger.config(bodyText?.joinToString(";; "))
//                    this.bodyText[country] = bodyText
//                    this.urls[country] = result?.getElementsByClass("Calendar-linkImg")?.map { it.attr("href") }
                }

                episodesList.forEach {
                    it.platformImpl = Jais.getPlatformInformation(this)
                    it.countryImpl = Jais.getCountryInformation(country)
                }

                episodesList.filter {
                    !this.checkedEpisodes.contains(it.episodeId.toString()) && it.isValid() && ISO8601.isSameDayUsingISO8601(
                        ISO8601.fromCalendar1(it.releaseDate),
                        ISO8601.fromCalendar(calendar)
                    ) && calendar.after(ISO8601.toCalendar1(it.releaseDate))
                }.forEachIndexed { _, wakanimEpisode ->
                    val episode = wakanimEpisode.toEpisode() ?: return@forEachIndexed
                    list.add(episode)

                    this.addCheckEpisodes(wakanimEpisode.episodeId!!.toString())
                    this.addCheckEpisodes(
                        Base64.getEncoder()
                            .encodeToString("${wakanimEpisode.releaseDate}${wakanimEpisode.anime}${wakanimEpisode.number}${wakanimEpisode.episodeType}${wakanimEpisode.langType}".encodeToByteArray())
                    )
                }
            }
        }

        return list.toTypedArray()
    }

    private fun getDate(calendar: Calendar): String = SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}