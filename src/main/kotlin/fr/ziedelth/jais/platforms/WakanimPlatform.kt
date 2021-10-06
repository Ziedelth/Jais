/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType
import fr.ziedelth.jais.utils.animes.episodes.platforms.WakanimEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.pow

@PlatformHandler(
    name = "Wakanim",
    url = "https://wakanim.tv/",
    image = "images/wakanim.jpg",
    color = 0xE3474B,
    countries = [FranceCountry::class]
)
class WakanimPlatform : Platform() {
    private val options = ChromeOptions().setHeadless(true).setProxy(null)
    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    override fun checkLastNews() {
        TODO("Not yet implemented")
    }

    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformHandler = Jais.getPlatformInformation(this)?.platformHandler
        val list = mutableListOf<Episode>()
        val date = getDate(calendar)

        this.getAllowedCountries().forEach { country ->
            var driver: ChromeDriver? = null

            try {
                /*
                With proxy : ~2895ms
                Without proxy : ~3238ms, ~2997ms, ~2973ms
                 */
                Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF
                driver = ChromeDriver(this.service, this.options)
                driver.manage().timeouts().pageLoadTimeout(60L, TimeUnit.SECONDS)
                driver.manage().timeouts().setScriptTimeout(60L, TimeUnit.SECONDS)
                val wait = WebDriverWait(driver, 10L)
                val episodesList = mutableListOf<WakanimEpisode>()

                driver.get("https://www.wakanim.tv/${country.checkOnEpisodesURL(this)}/v2/agenda/getevents?s=$date&e=$date&free=false")

                // TODO: Visibility, presence or findElement ?
                /*
                Presence : ~58,5ms
                Visibility : ~88,4ms
                FindElement : ~68ms
                 */
                val bodyText =
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))?.text?.split("\n")
                        ?.toMutableList()
                bodyText?.removeAt(0)

                if (!bodyText.isNullOrEmpty()) {
                    if (bodyText[0].equals("Pas de nouveaux épisodes !", true)) return@forEach
                    val urls =
                        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("Calendar-linkImg")))
                            ?.map { it.getAttribute("href") }

                    if (!urls.isNullOrEmpty()) {
                        for (i in 0 until bodyText.indexOfFirst {
                            it.equals("Aujourd'hui", true) || it[max(
                                0,
                                it.length - 3
                            )] == '/'
                        } step 4) {
                            val episodeText = bodyText.subList(i, i + 4).joinToString().split(",")
                            val tas = episodeText[0].split(" ")

                            val time = ISO8601.fromCalendar1("${this.getISODate(calendar)}T${tas[0]}:00Z")
                            if (calendar.before(ISO8601.toCalendar1(time))) continue
                            val anime = episodeText.subList(0, episodeText.size - 3).joinToString(",").split(" ")
                                .filterIndexed { index, _ -> index != 0 }.joinToString(" ")
                            val number = episodeText[episodeText.size - 2].replace(" ", "").toLongOrNull()
                            val episodeType =
                                if (Jais.getCountryInformation(country)!!.countryHandler.film == episodeText[episodeText.size - 3].split(
                                        " "
                                    )[2]
                                ) EpisodeType.FILM else EpisodeType.EPISODE
                            val langType = LangType.getLangType(episodeText[episodeText.size - 1].replace(" ", ""))
                            val url = urls[i / 4]
                            val wakanimType = url.split("/")[6]

                            driver.get(url)

                            try {
                                driver.findElementByClassName("css-1fxzzmg")?.click()
                            } catch (exception: Exception) {
                            }

                            val cardEpisodeElement: WebElement? = if (wakanimType.equals("episode", true)) {
                                // IN EPISODES
                                wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        By.className("slider_list"),
                                        By.className("currentEp")
                                    )
                                ).firstOrNull()
                            } else {
                                try {
                                    if (driver.findElementByClassName("NoEpisodes") != null) continue
                                } catch (exception: Exception) {
                                }
                                wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        By.className("list-episodes-container"),
                                        By.className("slider_item")
                                    )
                                ).lastOrNull()
                            }

                            if (cardEpisodeElement == null) continue

                            val cardElements = cardEpisodeElement.text?.split("\n")
                            val cardNumber = cardElements?.get(0)?.toLongOrNull()

                            if (number != null && cardNumber != null && number == cardNumber) {
                                val cardUrl = wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        cardEpisodeElement,
                                        By.className("slider_item_star")
                                    )
                                )?.lastOrNull()?.getAttribute("href")
                                val image = wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        cardEpisodeElement,
                                        By.tagName("img")
                                    )
                                )?.lastOrNull()?.getAttribute("src")

                                val episodeId = cardUrl?.split("/")?.get(7)?.toLongOrNull()
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

                                episodesList.add(
                                    WakanimEpisode(
                                        releaseDate = time,
                                        anime = anime,
                                        season = season,
                                        number = number,
                                        episodeType = episodeType,
                                        langType = langType,
                                        episodeId = episodeId,
                                        image = image,
                                        duration = duration,
                                        url = url
                                    )
                                )
                            }
                        }
                    }
                }

                episodesList.forEach { it.platform = this; it.country = country }
                episodesList.filter { it.isValid() && calendar.after(ISO8601.toCalendar1(it.releaseDate)) }
                    .sortedBy { ISO8601.toCalendar1(it.releaseDate) }.mapNotNull { it.toEpisode() }
                    .let { list.addAll(it) }
            } catch (exception: Exception) {
                JLogger.log(
                    Level.SEVERE,
                    "Failed to get ${this.javaClass.simpleName} episode(s) : ${exception.message}",
                    exception
                )
            } finally {
                driver?.quit()
            }
        }

        return list.toTypedArray()
    }

    private fun getDate(calendar: Calendar): String = SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}