/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.*
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
        val date = getDate(calendar)

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
                }

                this.lastDate = date
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Error on get all calendars episode", exception)
            } finally {
                driver.quit()
            }
        }

        this.getAllowedCountries().filter { country ->
            val fc = this.calendars.getOrDefault(country, mutableListOf()).firstOrNull()
            !fc.isNullOrEmpty() && calendar.after(ISO8601.toCalendar(fc))
        }.forEach { country ->
            val f = this.calendars.getOrDefault(country, mutableListOf())
            f.removeFirst()

            val driverArray = setDriver()
            val driver = driverArray[0] as ChromeDriver
            val driverWait = driverArray[1] as WebDriverWait

            try {
                driver.get("${this.getURL()}${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")
                val episodeList =
                    driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[1]/div[2]/div")))

                if (!(episodeList.size == 1 && episodeList[0].text == "Pas de nouveaux épisodes !")) {
                    val list: MutableList<EpisodeBuilder> = mutableListOf()

                    episodeList.forEachIndexed { index, webElement ->
                        val episodeBuilder = EpisodeBuilder(this, country)
                        val fullText = webElement.text.replace("\n", " ")
                        val splitFullText = fullText.split(" ")
                        val time = splitFullText[0]

                        val releaseDate = setDate(date, time.split(":")[0].toInt(), time.split(":")[1].toInt())
                        episodeBuilder.calendar = releaseDate
                        if (calendar.before(releaseDate)) return@forEachIndexed

                        val url =
                            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[${(index + 1)}]/div[2]/a")))
                                .getAttribute("href")
                        episodeBuilder.url = url
                        val tt = url?.replace("//", "/")?.split("/")?.get(5)

                        val anime = StringBuilder()
                        val max = splitFullText.indexOf("Séries") - 1
                        for (i in 1..max) anime.append(splitFullText[i]).append(if (i >= max) "" else " ")
                        episodeBuilder.anime = anime.toString()
                        val number = Const.toInt(splitFullText[splitFullText.size - 2])
                        episodeBuilder.number = number
                        val type = if (splitFullText[splitFullText.size - 1].equals(
                                country.dubbed,
                                true
                            )
                        ) EpisodeType.DUBBED else EpisodeType.SUBTITLED
                        episodeBuilder.type = type

                        if (tt.equals("episode", true)) {
                            val image =
                                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[${(index + 1)}]/div[2]/a/img")))
                                    .getAttribute("src")
                            episodeBuilder.image = image
                            episodeBuilder.episodeId = url?.replace("//", "/")?.split("/")?.get(6)?.toLong() ?: 0
                        }

                        list.add(episodeBuilder)
                    }

                    list.filter { calendar.after(it.calendar) }.forEach {
                        val tt = it.url?.replace("//", "/")?.split("/")?.get(5)

                        if (tt.equals("show", true)) {
                            try {
                                driver.get(it.url)

                                val episodes = driverWait.until(
                                    ExpectedConditions.visibilityOfAllElementsLocatedBy(
                                        By.className("slider_item_inner")
                                    )
                                )
                                val webElement = episodes.lastOrNull()

                                val numberCheck = driverWait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        webElement,
                                        By.className("slider_item_number")
                                    )
                                )[0].text?.replace(" ", "")?.toInt()

                                if (it.number.equals("$numberCheck", true)) {
                                    val urlElement = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            webElement,
                                            By.className("slider_item_star")
                                        )
                                    )[0]
                                    it.url = urlElement.getAttribute("href")
                                    it.episodeId =
                                        it.url?.replace("//", "/")?.split("/")?.get(6)?.toLong() ?: 0
                                    val image = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            webElement,
                                            By.tagName("img")
                                        )
                                    )?.get(0)?.getAttribute("src")
                                    it.image = image
                                    val season = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            webElement,
                                            By.className("slider_item_season")
                                        )
                                    )[0].text
                                    it.season = if (season.contains(country.season, true)) {
                                        val splitted = season.split(" ")
                                        splitted[splitted.indexOf(country.season) + 1]
                                    } else "1"
                                    val time = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            webElement,
                                            By.className("slider_item_duration")
                                        )
                                    )[0].text.split(":")

                                    var duration: Long = 0
                                    val dl = time.size
                                    for (i in dl downTo 1) duration += (time[dl - i].ifEmpty { "0" }).toLong() * 60.0.pow(
                                        ((i - 1).toDouble())
                                    ).toLong()

                                    it.duration = duration
                                }
                            } catch (exception: Exception) {
                                JLogger.log(
                                    Level.WARNING,
                                    "Error on get Wakanim more infos episode",
                                    exception
                                )
                            }
                        } else if (tt.equals("episode", true)) {
                            try {
                                driver.get(it.url)

                                val episodes = driverWait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        By.className("currentEp"), By.className("slider_item_inner")
                                    )
                                )
                                val webElement = episodes.lastOrNull()

                                val numberCheck = driverWait.until(
                                    ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                        webElement,
                                        By.className("slider_item_number")
                                    )
                                )[0].text?.replace(" ", "")?.toInt()

                                if (it.number.equals("$numberCheck", true)) {
                                    val season = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            webElement,
                                            By.className("slider_item_season")
                                        )
                                    )[0].text
                                    it.season = if (season.contains(country.season, true)) {
                                        val splitted = season.split(" ")
                                        splitted[splitted.indexOf(country.season) + 1]
                                    } else "1"
                                    val time = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            webElement,
                                            By.className("slider_item_duration")
                                        )
                                    )[0].text.split(":")

                                    var duration: Long = 0
                                    val dl = time.size
                                    for (i in dl downTo 1) duration += (time[dl - i].ifEmpty { "0" }).toLong() * 60.0.pow(
                                        ((i - 1).toDouble())
                                    ).toLong()

                                    it.duration = duration
                                }
                            } catch (exception: Exception) {
                                JLogger.log(
                                    Level.WARNING,
                                    "Error on get Wakanim more infos episode",
                                    exception
                                )
                            }
                        }

                        l.add(
                            Episode(
                                platform = this,
                                calendar = ISO8601.fromCalendar(it.calendar!!),
                                anime = it.anime!!,
                                number = it.number!!,
                                country = country,
                                type = it.type!!,
                                season = it.season!!,
                                episodeId = it.episodeId,
                                title = null,
                                image = it.image,
                                url = it.url,
                                duration = it.duration
                            )
                        )
                    }
                }
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Error on get Wakanim episode", exception)
            } finally {
                driver.quit()
            }
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