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
import fr.ziedelth.jais.utils.animes.episodes.platforms.WakanimEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.ProtocolHandshake
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.pow

@PlatformHandler(
    name = "Wakanim",
    url = "https://wakanim.tv/",
    image = "https://ziedelth.fr/images/wakanim.png",
    color = 0xE3474B,
    countries = [FranceCountry::class]
)
class WakanimPlatform : Platform() {
    private val options = ChromeOptions().setHeadless(true)
    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    override fun checkLastNews() {
        TODO("Not yet implemented")
    }

    override fun checkLastEpisodes(): Array<Episode> {
        val list = mutableListOf<Episode>()
        val calendar = Calendar.getInstance()
        val date = getDate(calendar)

        JLogger.info("Fetch ${this.javaClass.simpleName} episode(s)")
        val start = System.currentTimeMillis()

        this.getAllowedCountries().forEach { country ->
            var driver: ChromeDriver? = null

            try {
                Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF
                driver = ChromeDriver(this.service, this.options)

                driver.get("https://www.wakanim.tv/${country.checkOnEpisodesURL(this)}/v2/agenda/getevents?s=$date&e=$date&free=false")

                val bodyText = driver.findElement(By.tagName("body")).text.split("\n")
                val urls = driver.findElements(By.className("Calendar-linkImg")).map { it.getAttribute("href") }
                var episodesList = mutableListOf<WakanimEpisode>()

                for (i in 1 until bodyText.indexOfFirst { it[max(0, it.length - 3)] == '/' } step 4) {
                    val episodeText = bodyText.subList(i, i + 4).joinToString().split(",")
                    val tas = episodeText[0].split(" ")
                    val time = ISO8601.fromCalendar1("${this.getISODate(calendar)}T${tas[0]}:00Z")
                    val anime = tas.subList(1, tas.size).joinToString(" ")
                    val number = episodeText[2].replace(" ", "")
                    val episodeType = EpisodeType.getEpisodeType(episodeText[3].replace(" ", ""))
                    val url = urls[(i - 1) / 4]
                    val wakanimType = url?.split("/")?.get(6)

                    driver.get(url)

                    val cardEpisodeElement = if (wakanimType.equals(
                            "episode",
                            true
                        )
                    ) driver.findElement(By.xpath("//div[@class='slider_list currentEp']")) else driver.findElements(
                        By.xpath(
                            "//div[@class='list-episodes-container slider_item']"
                        )
                    ).lastOrNull()
                    val cardNumber =
                        cardEpisodeElement?.findElement(By.className("slider_item_number"))?.text?.replace(" ", "")

                    if (number == cardNumber) {
                        val cardUrl =
                            cardEpisodeElement.findElement(By.className("slider_item_star"))?.getAttribute("href")
                        val episodeId = cardUrl?.split("/")?.get(7)?.toLongOrNull()
                        val cardSeason = cardEpisodeElement.findElement(By.className("slider_item_season"))?.text
                        val season = if (cardSeason?.contains(
                                Jais.getCountryInformation(country)!!.countryHandler.season,
                                true
                            ) == true
                        ) {
                            val split = cardSeason.split(" ")
                            split[split.indexOf((Jais.getCountryInformation(country)!!.countryHandler.season)) + 1]
                        } else "1"
                        val image = cardEpisodeElement.findElement(By.tagName("img"))?.getAttribute("src")
                        val cardDuration =
                            cardEpisodeElement.findElement(By.className("slider_item_duration"))?.text?.split(":")
                        val duration = cardDuration?.reversed()?.mapIndexed { it, t ->
                            (t.ifEmpty { "0" }.toLongOrNull()?.times(60.0.pow((it - 1).toDouble())) ?: 0L).toLong()
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
                                episodeId = episodeId,
                                image = image,
                                duration = duration,
                                url = url
                            )
                        )
                    }
                }

                episodesList = episodesList.filter {
                    it.isValid() && ISO8601.isSameDayUsingInstant(
                        calendar,
                        ISO8601.toCalendar1(it.releaseDate)
                    ) && calendar.after(ISO8601.toCalendar1(it.releaseDate))
                }.sortedBy { ISO8601.toCalendar1(it.releaseDate) }.toMutableList()

                JLogger.config("${episodesList.size}")
                JLogger.config(
                    episodesList.mapNotNull { ISO8601.fromCalendar1(it.releaseDate) }.distinct().toTypedArray()
                        .contentToString()
                )
                JLogger.config("$episodesList")
                JLogger.config("Fetch in ${System.currentTimeMillis() - start}ms")

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