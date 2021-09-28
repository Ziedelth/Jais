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
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.pow

@PlatformHandler(
    name = "Wakanim",
    url = "https://wakanim.tv/",
    image = "images/wakanim.png",
    color = 0xE3474B,
    countries = [FranceCountry::class]
)
class WakanimPlatform : Platform() {
    private val options = ChromeOptions().setHeadless(true)
    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    override fun checkLastNews() {
        TODO("Not yet implemented")
    }

    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val list = mutableListOf<Episode>()
        val date = getDate(calendar)

        this.getAllowedCountries().forEach { country ->
            var driver: ChromeDriver? = null

            try {
                Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF
                driver = ChromeDriver(this.service, this.options)
                val wait = WebDriverWait(driver, 10L)

                driver.get("https://www.wakanim.tv/${country.checkOnEpisodesURL(this)}/v2/agenda/getevents?s=$date&e=$date&free=false")

                val bodyText =
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")))?.text?.split("\n")
                        ?.toMutableList()
                bodyText?.removeAt(0)
                var episodesList = mutableListOf<WakanimEpisode>()

                if (!bodyText.isNullOrEmpty()) {
                    if (bodyText[0] == "Pas de nouveaux épisodes !") return@forEach
                    val urls =
                        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("Calendar-linkImg")))
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
                            val anime = tas.subList(1, tas.size).joinToString(" ")
                            val number = episodeText[2].replace(" ", "").toLongOrNull()
                            val episodeType =
                                if (Jais.getCountryInformation(country)!!.countryHandler.film == episodeText[1].split(" ")[2]) EpisodeType.FILM else EpisodeType.EPISODE
                            val langType = LangType.getLangType(episodeText[3].replace(" ", ""))
                            val url = urls[i / 4]
                            val wakanimType = url.split("/")[6]

                            driver.get(url)

                            val cardEpisodeElement = if (wakanimType.equals(
                                    "episode",
                                    true
                                )
                            ) wait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    By.className("slider_list"),
                                    By.className("currentEp")
                                )
                            ).firstOrNull() else wait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    By.className(
                                        "list-episodes-container"
                                    ), By.className("slider_item")
                                )
                            ).lastOrNull()
                            val cardNumber = wait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    cardEpisodeElement,
                                    By.className("slider_item_number")
                                )
                            )?.lastOrNull()?.text?.replace(" ", "")?.toLongOrNull()

                            if (number != null && cardNumber != null && number == cardNumber) {
                                val cardUrl = wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        cardEpisodeElement,
                                        By.className("slider_item_star")
                                    )
                                )?.lastOrNull()?.getAttribute("href")
                                val episodeId = cardUrl?.split("/")?.get(7)?.toLongOrNull()
                                val cardSeason = wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        cardEpisodeElement,
                                        By.className("slider_item_season")
                                    )
                                )?.lastOrNull()?.text
                                val season = if (cardSeason?.contains(
                                        Jais.getCountryInformation(country)!!.countryHandler.season,
                                        true
                                    ) == true
                                ) {
                                    val split = cardSeason.split(" ")
                                    split[split.indexOf((Jais.getCountryInformation(country)!!.countryHandler.season)) + 1].toLongOrNull()
                                } else 1L
                                val image = wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        cardEpisodeElement,
                                        By.tagName("img")
                                    )
                                )?.lastOrNull()?.getAttribute("src")
                                val cardDuration = wait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        cardEpisodeElement,
                                        By.className("slider_item_duration")
                                    )
                                )?.lastOrNull()?.text?.split(":")
                                val duration = cardDuration?.mapIndexed { it, t ->
                                    (t.ifEmpty { "0" }.toLongOrNull()
                                        ?.times(60.0.pow(((cardDuration.size - it) - 1).toDouble())) ?: 0L).toLong()
                                }?.sum()

                                episodesList.add(
                                    WakanimEpisode(
                                        platform = this,
                                        country = country,
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

                episodesList = episodesList.filter {
                    it.isValid() && calendar.after(ISO8601.toCalendar1(it.releaseDate))
                }.sortedBy { ISO8601.toCalendar1(it.releaseDate) }.toMutableList()

                episodesList.mapNotNull { it.toEpisode() }.let { list.addAll(it) }
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