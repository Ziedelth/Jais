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
import fr.ziedelth.jais.utils.WebDriverBuilder
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
import fr.ziedelth.jais.utils.animes.episodes.platforms.WakanimEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.debug.JLogger
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

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
    private val bodyText = mutableMapOf<Country, MutableList<String>?>()
    private val urls = mutableMapOf<Country, List<String>?>()

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
            var webDriverImpl: WebDriverBuilder.WebDriverImpl? = null

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val episodesList = mutableListOf<WakanimEpisode>()

                if (System.currentTimeMillis() - (this.lastCheck[country] ?: 0) >= 3600000) {
                    this.lastCheck[country] = System.currentTimeMillis()

                    if (webDriverImpl == null) webDriverImpl = WebDriverBuilder.setDriver()
                    val url =
                        "https://www.wakanim.tv/${country.checkOnEpisodesURL(this)}/v2/agenda/getevents?s=$date&e=$date&free=false"
                    webDriverImpl?.driver?.get(url)
                    if (hasBotDetection(webDriverImpl)) return@tryCatch
                    Thread.currentThread().join(Random.nextLong(10000, 15000))

                    val bodyText =
                        webDriverImpl?.wait?.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))?.text?.split(
                            "\n"
                        )?.toMutableList()
                    bodyText?.removeAt(0)
                    this.bodyText[country] = bodyText
                    this.urls[country] =
                        webDriverImpl?.wait?.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("Calendar-linkImg")))
                            ?.map { it.getAttribute("href") }
                }

                if (!this.bodyText[country].isNullOrEmpty()) {
                    if (this.bodyText[country]?.get(0).equals("Pas de nouveaux épisodes !", true)) return@tryCatch

                    if (!this.urls[country].isNullOrEmpty()) {
                        for (i in 0 until (this.bodyText[country]?.indexOfFirst {
                            it.equals(
                                "Aujourd'hui",
                                true
                            ) || it[max(0, it.length - 3)] == '/'
                        } ?: 0) step 4) {
                            val episodeText = this.bodyText[country]?.subList(i, i + 4)?.joinToString()?.split(",")
                            val tas = episodeText?.get(0)?.split(" ")
                            val time = ISO8601.fromCalendar1("${this.getISODate(calendar)}T${tas?.get(0)}:00Z")
                            if (calendar.before(ISO8601.toCalendar1(time))) continue
                            val anime = episodeText?.subList(0, episodeText.size - 3)?.joinToString(",")?.split(" ")
                                ?.filterIndexed { index, _ -> index != 0 }?.joinToString(" ")
                            val number = episodeText?.get(episodeText.size - 2)?.replace(" ", "")?.toLongOrNull()

                            val episodeType =
                                if (EpisodeType.FILM.getData(countryInformation?.country?.javaClass)?.data == (episodeText?.get(
                                        episodeText.size - 3
                                    )?.split(" ")?.get(2) ?: "")
                                ) EpisodeType.FILM else EpisodeType.EPISODE
                            val langType =
                                LangType.getLangType(episodeText?.get(episodeText.size - 1)?.replace(" ", ""))
                            if (langType == LangType.UNKNOWN) continue
                            val url = this.urls[country]?.get(i / 4)
                            val wakanimType = url?.split("/")?.get(6)

                            if (wakanimType.equals("episode", true)) {
                                val hash = Base64.getEncoder()
                                    .encodeToString("$time$anime$number$episodeType$langType".encodeToByteArray())
                                val episodeId = url?.split("/")?.get(7)
                                if (episodeId == null || (this.checkedEpisodes.contains(hash) || this.checkedEpisodes.contains(
                                        episodeId
                                    ))
                                ) continue
                            } else {
                                val episodeId = Base64.getEncoder()
                                    .encodeToString("$time$anime$number$episodeType$langType".encodeToByteArray())
                                if (episodeId == null || this.checkedEpisodes.contains(episodeId)) continue
                            }

                            webDriverImpl?.driver?.quit()
                            webDriverImpl = null
                            webDriverImpl = WebDriverBuilder.setDriver()
                            webDriverImpl?.driver?.get(url)
                            if (hasBotDetection(webDriverImpl)) continue
                            Thread.currentThread().join(Random.nextLong(10000, 15000))

                            val cardEpisodeElement: WebElement? = if (wakanimType.equals("episode", true)) {
                                // IN EPISODES
                                webDriverImpl?.wait?.until(
                                    ExpectedConditions.presenceOfNestedElementsLocatedBy(
                                        By.className(
                                            "slider_list"
                                        ), By.className("currentEp")
                                    )
                                )?.firstOrNull()
                            } else {
                                try {
                                    if (webDriverImpl?.driver?.findElement(By.className("NoEpisodes")) != null) continue
                                } catch (exception: Exception) {
                                }

                                webDriverImpl?.wait?.until(
                                    ExpectedConditions.presenceOfNestedElementsLocatedBy(
                                        By.className(
                                            "list-episodes-container"
                                        ), By.className("slider_item")
                                    )
                                )?.lastOrNull()
                            }

                            if (cardEpisodeElement == null) continue

                            val cardElements = cardEpisodeElement.text?.split("\n")
                            val cardNumber = cardElements?.get(0)?.toLongOrNull()

                            if (number != null && cardNumber != null && number == cardNumber) {
                                val cardUrl = webDriverImpl?.wait?.until(
                                    ExpectedConditions.presenceOfNestedElementLocatedBy(
                                        cardEpisodeElement,
                                        By.className("slider_item_star")
                                    )
                                )?.getAttribute("href")
                                val episodeId = cardUrl?.split("/")?.get(7)
                                if (episodeId == null || this.checkedEpisodes.contains(episodeId)) continue
                                val image = webDriverImpl?.wait?.until(
                                    ExpectedConditions.presenceOfNestedElementLocatedBy(
                                        cardEpisodeElement,
                                        By.tagName("img")
                                    )
                                )?.getAttribute("src")
                                val cardSeason = cardElements[cardElements.size - 1]
                                val season = if (cardSeason.contains(
                                        Jais.getCountryInformation(country)!!.countryHandler.season,
                                        true
                                    )
                                ) {
                                    val split = cardSeason.split(" ")
                                    split[split.indexOf((Jais.getCountryInformation(country)!!.countryHandler.season)) + 1].toLongOrNull()
                                } else 1L
                                val cardDuration = cardElements[1].split(":")
                                val duration = cardDuration.mapIndexed { it, t ->
                                    (t.ifEmpty { "0" }.toLongOrNull()
                                        ?.times(60.0.pow(((cardDuration.size - it) - 1).toDouble())) ?: 0L).toLong()
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
                    }
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

            webDriverImpl?.driver?.quit()
        }

        return list.toTypedArray()
    }

    private fun getDate(calendar: Calendar): String = SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

    private fun hasBotDetection(webDriverImpl: WebDriverBuilder.WebDriverImpl?): Boolean = try {
        if (webDriverImpl?.driver?.findElement(By.id("main-iframe")) != null) {
            JLogger.warning("Detected as bot for ${this.javaClass.simpleName} for ${webDriverImpl.driver.currentUrl}")
            true
        } else {
            false
        }
    } catch (exception: Exception) {
        false
    }
}