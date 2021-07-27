package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.*
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.pow

class Wakanim : Platform {
    private val options: FirefoxOptions = FirefoxOptions().setHeadless(true)

    init {
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true")
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null")
    }

    override fun getName(): String = "Wakanim"
    override fun getURL(): String = "https://www.wakanim.tv/"
    override fun getImage(): String =
        "https://play-lh.googleusercontent.com/J5_U63e4nJPrSUHeqqGIoZIaqQ1EYKEeXpcNaVbf95adUu9O9VnEgXC_ejUZPaCjpw"

    override fun getColor(): Color = Color(227, 71, 75)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()

    override fun getLastEpisodes(): Array<Episode> {
        val l: MutableList<Episode> = mutableListOf()
        val calendar = Calendar.getInstance()
        val date = this.date(calendar)

        Country.values().filter { this.getAllowedCountries().contains(it) }.forEach { country ->
            Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF

            val driver = FirefoxDriver(this.options)
            val wait = WebDriverWait(driver, 60)

            try {
                driver.get("https://www.wakanim.tv/${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")
                val a: List<WebElement?>? =
                    driver.findElement(By.className("Calendar-today")).findElements(By.className("Calendar-ep"))

                if (!a.isNullOrEmpty()) {
                    val list = aM(wait)

                    list?.forEach {
                        val time: String? = aPS(wait, it, "Calendar-hourTxt")?.text
                        val linkElement: WebElement? = aPS(wait, it, "Calendar-linkImg")

                        if (linkElement != null) {
                            val link = linkElement.getAttribute("href")

                            if (!time.isNullOrEmpty()) {
                                val releaseDate = setDate(time.split(":")[0].toInt(), time.split(":")[1].toInt())

                                if (calendar.after(releaseDate)) {
                                    val anime = aPS(wait, it, "Calendar-epTitle")!!.text
                                    val image = aPS(wait, linkElement, "Calendar-image")!!.getAttribute("src")
                                    val number = Const.toInt(aPS(wait, linkElement, "Calendar-epNumber")!!.text)
                                    val ltype = aPS(wait, it, "Calendar-tagTranslation")!!.text
                                    val episodeType = if (ltype.equals(
                                            country.dubbed,
                                            true
                                        )
                                    ) EpisodeType.DUBBED else EpisodeType.SUBTITLED
                                    val id = link.replace("//", "/").split("/")[6]
                                    val tt = link.replace("//", "/").split("/")[5]
                                    var duration: Long = 0

                                    if (tt.equals("episode", true)) {
                                        Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF

                                        val driverEpisode = FirefoxDriver(this.options)

                                        driverEpisode.get(link)

                                        val d = driverEpisode.findElement(By.className("currentEp"))
                                            .findElement(By.className("slider_item_inner"))
                                            .findElement(By.className("slider_item_resolution"))
                                            .findElement(By.className("slider_item_duration")).text

                                        val ds = d.split(":")
                                        val dl = ds.size
                                        for (i in dl downTo 1) duration += ds[dl - i].toLong() * 60.0.pow(((i - 1).toDouble()))
                                            .toLong()

                                        driverEpisode.quit()
                                    } else duration = 1440

                                    val episode = Episode(
                                        platform = this.getName(),
                                        calendar = ISO8601.fromCalendar(releaseDate),
                                        anime = anime,
                                        number = number,
                                        country = country,
                                        type = episodeType,
                                        id = id,
                                        title = null,
                                        image = image,
                                        link = link,
                                        duration = duration
                                    )
                                    episode.p = this
                                    l.add(episode)

                                    Thread.currentThread().join(5000)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                return l.toTypedArray()
            } finally {
                driver.quit()
            }
        }

        return l.toTypedArray()
    }

    private fun date(calendar: Calendar): String {
        return SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    }

    private fun setDate(hour: Int, minutes: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour + 2)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    private fun aS(wait: WebDriverWait, sClass: String): WebElement? {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.className(sClass)))
    }

    private fun aPS(wait: WebDriverWait, parent: WebElement, sClass: String): WebElement? {
        return wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(parent, By.className(sClass)))
    }

    private fun aPS(wait: WebDriverWait, parent: String, sClass: String): WebElement? {
        return wait.until(
            ExpectedConditions.presenceOfNestedElementLocatedBy(
                By.className(parent),
                By.className(sClass)
            )
        )
    }

    private fun aM(wait: WebDriverWait): List<WebElement>? {
        return wait.until(
            ExpectedConditions.presenceOfNestedElementsLocatedBy(
                By.className("Calendar-today"),
                By.className("Calendar-ep")
            )
        )
    }
}