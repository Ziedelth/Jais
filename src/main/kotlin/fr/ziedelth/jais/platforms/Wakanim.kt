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
import java.util.stream.Collectors
import kotlin.collections.set
import kotlin.math.pow

class Wakanim : Platform {
    private val timeout = 60L
    private val options = ChromeOptions().setHeadless(true)
    private val service = ChromeDriverService.Builder().withSilent(true).build() as ChromeDriverService

    private val calendars: MutableMap<Country, MutableList<String>> = mutableMapOf()
    private var lastDate: String? = null

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

        if (this.lastDate != date) {
            val driverArray = setDriver()
            val driver = driverArray[0] as ChromeDriver
            val driverWait = driverArray[1] as WebDriverWait

            try {
                this.calendars.clear()

                this.getAllowedCountries().forEach { country ->
                    driver.get("${this.getURL()}${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")
                    val episodeList =
                        driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[1]/div[2]/div")))

                    if (!(episodeList.size == 1 && episodeList[0].text == "Pas de nouveaux épisodes !")) {
                        this.calendars[country] = episodeList.map { webElement ->
                            val fullText = webElement.text.replace("\n", " ")
                            val splitFullText = fullText.split(" ")
                            val time = splitFullText[0]
                            ISO8601.fromCalendar(setDate(date, time.split(":")[0].toInt(), time.split(":")[1].toInt()))
                        }.stream().distinct().collect(Collectors.toList()).toMutableList()
                    }

                    if (!this.calendars[country].isNullOrEmpty()) JLogger.warning("[${country.country.uppercase()}] First episode on ${this.getName()} will available at : ${this.calendars[country]!!.first()}")
                    else JLogger.warning("[${country.country.uppercase()}] No episode today on ${this.getName()}")
                }

                this.lastDate = date
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Error on get all calendars episode", exception)
            } finally {
                driver.quit()
            }
        }

        this.calendars.forEach { (country, calendars) ->
            val filter = calendars.filter { calendar.after(ISO8601.toCalendar(it)) }
            if (filter.isEmpty()) return@forEach

            val driverArray = setDriver()
            val driver = driverArray[0] as ChromeDriver
            val driverWait = driverArray[1] as WebDriverWait

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
                        if (!filter.contains(ISO8601.fromCalendar(timeReleaseCalendar))) return@forEachIndexed
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
                        )[0].text
                        list.add("${ISO8601.fromCalendar(timeReleaseCalendar)}|$link|$tempNumber")
                    }

                list.forEach { stocked ->
                    val split = stocked.split("|")
                    val timeRelease = split[0]
                    val linkL = split[1]
                    val tempNumber = split[2]
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
                    )[0].text.replace(" ", "")

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
                            if (episodeType.equals(country.dubbed, true)) EpisodeType.DUBBED else EpisodeType.SUBTITLED
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
                        val link = driverWait.until(
                            ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                webElementEpisode,
                                By.className("slider_item_link")
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
                                this,
                                timeRelease,
                                anime,
                                number,
                                country,
                                type,
                                season,
                                episodeId.toLong(),
                                null,
                                image,
                                link,
                                duration
                            )
                        )
                    }
                }
            }

            val lr = ArrayList(calendars)
            lr.removeAll(filter)

            this.calendars.replace(country, lr)
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