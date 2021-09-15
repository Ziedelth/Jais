/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Platform
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.set
import kotlin.math.pow

class Wakanim : Platform {
    private val timeout = 60L
    private val options = ChromeOptions().setHeadless(true)
    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    private val calendars: MutableMap<Country, MutableList<Calendar>> = mutableMapOf()
    private val counter: MutableMap<String, Long> = mutableMapOf()
    private var lastCheck: Long = 0

    override fun getName(): String = "Wakanim"
    override fun getURL(): String = "https://www.wakanim.tv/"
    override fun getImage(): String = "https://ziedelth.fr/images/wakanim.png"
    override fun getColor(): Color = Color(227, 71, 75)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    private fun setDriver(): Array<Any> {
        Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF
        val driver = ChromeDriver(this.service, this.options)
        driver.manage().timeouts().pageLoadTimeout(this.timeout, TimeUnit.SECONDS)
        driver.manage().timeouts().setScriptTimeout(this.timeout, TimeUnit.SECONDS)
        val driverWait = WebDriverWait(driver, this.timeout)
        return arrayOf(driver, driverWait)
    }

    override fun getLastEpisodes(): Array<Episode> {
        val l: MutableList<Episode> = mutableListOf()
        val calendar = Calendar.getInstance()
        val date = getFrenchDate(calendar)
        val list: MutableList<String> = mutableListOf()

        if ((System.currentTimeMillis() - this.lastCheck) >= 3600000) {
            val driverArray = setDriver()
            val driver = driverArray[0] as ChromeDriver
            val driverWait = driverArray[1] as WebDriverWait

            try {
                this.calendars.clear()
                this.counter.clear()

                this.getAllowedCountries().forEach { country ->
                    driver.get("${this.getURL()}${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")
                    val episodeList =
                        driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[1]/div[2]/div")))

                    if (!(episodeList.size == 1 && episodeList[0].text == "Pas de nouveaux épisodes !")) {
                        this.calendars[country] = episodeList.asSequence().map { webElement ->
                            val fullText = webElement.text.replace("\n", " ")
                            val splitFullText = fullText.split(" ")
                            val episodeCalendar = ISO8601.fromCalendar(
                                setDate(
                                    date,
                                    splitFullText[0].split(":")[0].toInt(),
                                    splitFullText[0].split(":")[1].toInt()
                                )
                            )
                            this.counter[episodeCalendar] = this.counter.getOrDefault(episodeCalendar, 0L) + 1
                            episodeCalendar
                        }.distinct().map { ISO8601.toCalendar(it) }.sortedBy { it }.toMutableList()
                    }

                    if (!this.calendars[country].isNullOrEmpty()) JLogger.info(
                        "[${country.country.uppercase()}] Episodes on ${this.getName()} will available at : ${
                            this.calendars[country]!!.map {
                                ISO8601.fromCalendar(
                                    it
                                )
                            }
                        }"
                    )
                    else JLogger.warning("[${country.country.uppercase()}] No episode today on ${this.getName()}")
                }

                this.lastCheck = System.currentTimeMillis()
            } catch (exception: Exception) {
                JLogger.log(Level.SEVERE, "Error on get all calendars episode", exception)
            } finally {
                driver.quit()
            }
        }

        this.calendars.forEach { (country, calendars) ->
            val filters = calendars.filter { calendar.after(it) }
            if (filters.isEmpty()) return@forEach
            val fs = filters.map { ISO8601.fromCalendar(it) }
            val lc: MutableList<Calendar> = mutableListOf()

            val driverArray = setDriver()
            val driver = driverArray[0] as ChromeDriver
            val driverWait = driverArray[1] as WebDriverWait

            try {
                driver.get("${this.getURL()}${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")
                val episodeList =
                    driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[1]/div[2]/div")))

                if (!(episodeList.size == 1 && episodeList[0].text == "Pas de nouveaux épisodes !")) {
                    driverWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("Calendar-ep")))
                        .forEachIndexed { _, webElement ->
                            val timeRelease = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElement,
                                    By.className("Calendar-hourTxt")
                                )
                            )[0].text
                            val timeReleaseCalendar =
                                setDate(date, timeRelease.split(":")[0].toInt(), timeRelease.split(":")[1].toInt())
                            if (!fs.contains(ISO8601.fromCalendar(timeReleaseCalendar))) return@forEachIndexed
                            val link = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElement,
                                    By.className("Calendar-linkImg")
                                )
                            )[0].getAttribute("href")
                            val tempNumber = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElement,
                                    By.className("Calendar-epNumber")
                                )
                            )[0].text.toInt()
                            list.add("${ISO8601.fromCalendar(timeReleaseCalendar)}|$link|$tempNumber")
                        }

                    list.forEach { stocked ->
                        val split = stocked.split("|")
                        val timeRelease = split[0]
                        val linkL = split[1]
                        val tempNumber = split[2].toInt()
                        driver.get(linkL)

                        val wakanimType = linkL.split("/")[6]
                        val webElementEpisode = if (wakanimType.equals(
                                "episode",
                                true
                            )
                        ) driverWait.until(
                            ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                By.className("slider_list"),
                                By.className("currentEp")
                            )
                        ).firstOrNull() else driverWait.until(
                            ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                By.className(
                                    "list-episodes-container"
                                ), By.className("slider_item")
                            )
                        ).lastOrNull()
                        val number = driverWait.until(
                            ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                webElementEpisode,
                                By.className("slider_item_number")
                            )
                        )[0].text.replace(" ", "").toInt()

                        if (number == tempNumber) {
                            val anime = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElementEpisode,
                                    By.className("slider_item_showTitle")
                                )
                            )[0].getAttribute("title")
                            val episodeType = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElementEpisode,
                                    By.className("slider_item_info_text")
                                )
                            )[0].text
                            val type =
                                if (episodeType.equals(
                                        country.dubbed,
                                        true
                                    )
                                ) EpisodeType.DUBBED else EpisodeType.SUBTITLED
                            val tempSeason = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElementEpisode,
                                    By.className("slider_item_season")
                                )
                            )[0].text
                            val season = if (tempSeason.contains(country.season, true)) {
                                val splitted = tempSeason.split(" ")
                                splitted[splitted.indexOf(country.season) + 1]
                            } else "1"

                            val episodeId = linkL.split("/")[7]
                            val image = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElementEpisode,
                                    By.tagName("img")
                                )
                            )[0].getAttribute("src")
                            val url = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElementEpisode,
                                    By.className("slider_item_star")
                                )
                            )[0].getAttribute("href")
                            val time = driverWait.until(
                                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                    webElementEpisode,
                                    By.className("slider_item_duration")
                                )
                            )[0].text.split(":")

                            var duration: Long = 0
                            val dl = time.size
                            for (i in dl downTo 1) duration += (time[dl - i].ifEmpty { "0" }).toLong() * 60.0.pow(((i - 1).toDouble()))
                                .toLong()

                            l.add(
                                Episode(
                                    platform = this,
                                    calendar = timeRelease,
                                    anime = anime,
                                    number = "$number",
                                    country = country,
                                    type = type,
                                    season = season,
                                    episodeId = episodeId.toLong(),
                                    title = null,
                                    image = image,
                                    url = url,
                                    duration = duration
                                )
                            )

                            if (list.count { fs.contains(timeRelease) }.toLong() >= (this.counter[timeRelease]
                                    ?: 0) && !lc.contains(ISO8601.toCalendar(timeRelease))
                            ) {
                                lc.add(ISO8601.toCalendar(timeRelease))
                                JLogger.info("Removing calendar : $timeRelease, all episodes is out!")
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                JLogger.log(Level.SEVERE, "Error on get all episode", exception)
            } finally {
                driver.quit()
            }

            calendars.removeAll(lc)
            this.calendars.replace(country, calendars)
        }

        return l.toTypedArray()
    }

    private fun setDate(date: String, hour: Int, minutes: Int): Calendar {
        val calendar = Calendar.getInstance()
        val timezone = ZonedDateTime.now().offset.totalSeconds
        val d = SimpleDateFormat("dd-MM-yyyy").parse(date)
        calendar.time = d

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.SECOND, timezone)
        return calendar
    }
}