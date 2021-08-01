package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.*
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.min
import kotlin.math.pow

class Wakanim : Platform {
    private val timeout = 60L
    private val options: FirefoxOptions = FirefoxOptions().setHeadless(true)
    private val episodes: MutableMap<String, Long> = mutableMapOf()

    init {
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true")
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null")
    }

    override fun getName(): String = "Wakanim"
    override fun getURL(): String = "https://www.wakanim.tv/"
    override fun getImage(): String =
        "https://ziedelth.fr/images/wakanim.png"

    override fun getColor(): Color = Color(227, 71, 75)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()

    override fun getLastEpisodes(): Array<Episode> {
        val l: MutableList<Episode> = mutableListOf()
        val calendar = Calendar.getInstance()
        val date = this.date(calendar)

        this.getAllowedCountries().forEach { country ->
            Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF

            val driver = FirefoxDriver(this.options)
            driver.manage().timeouts().pageLoadTimeout(this.timeout, TimeUnit.SECONDS)
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
            val driverWait = WebDriverWait(driver, this.timeout)

            try {
                driver.get("${this.getURL()}${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")

                val episodeList =
                    driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[1]/div[2]/div")))

                if (!(episodeList.size == 1 && episodeList[0].text == "Pas de nouveaux épisodes !")) {
                    episodeList.forEachIndexed { index, webElement ->
                        val fullText = webElement.text.replace("\n", " ")
                        val splitFullText = fullText.split(" ")

                        // UNCHANGED
                        val releaseDate =
                            setDate(splitFullText[0].split(":")[0].toInt(), splitFullText[0].split(":")[1].toInt())

                        if (calendar.after(releaseDate)) {
                            val linkElement =
                                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[${(index + 1)}]/div[2]/a")))
                            val link = linkElement.getAttribute("href")

                            // UNCHANGED// UNCHANGED
                            val anime = StringBuilder()
                            val max = splitFullText.indexOf("Séries") - 1
                            for (i in 1..max) anime.append(splitFullText[i]).append(if (i >= max) "" else " ")
                            val number = Const.toInt(splitFullText[splitFullText.size - 2])
                            val type = if (splitFullText[splitFullText.size - 1].equals(
                                    country.dubbed,
                                    true
                                )
                            ) EpisodeType.DUBBED else EpisodeType.SUBTITLED

                            // CHANGED
                            val image =
                                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[${(index + 1)}]/div[2]/a/img")))
                                    .getAttribute("src")
                            val id = link.replace("//", "/").split("/")[6]
                            val tt = link.replace("//", "/").split("/")[5]
                            var duration: Long = 0

                            if (tt.equals("episode", true)) {
                                if (!this.episodes.containsKey(id)) {
                                    JLogger.info("Check time for episode $anime...")
                                    Logger.getLogger(ProtocolHandshake::class.java.name).level = Level.OFF
                                    val driverEpisode = FirefoxDriver(this.options)
                                    driverEpisode.manage().timeouts().pageLoadTimeout(this.timeout, TimeUnit.SECONDS)
                                    driverEpisode.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                                    val driverEpisodeWait = WebDriverWait(driverEpisode, this.timeout)

                                    try {
                                        driverEpisode.get(link)

                                        val list =
                                            driverEpisodeWait.until(
                                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                    By.xpath(
                                                        "/html/body/section[2]/div/div/div[1]/div/ul/li"
                                                    )
                                                )
                                            )

                                        val time = driverEpisodeWait.until(
                                            ExpectedConditions.presenceOfElementLocated(
                                                By.xpath(
                                                    "/html/body/section[2]/div/div/div[1]/div/ul/li[${
                                                        min(
                                                            5,
                                                            list.size
                                                        )
                                                    }]/div/div[2]/span"
                                                )
                                            )
                                        ).text.split(":")

                                        val dl = time.size
                                        for (i in dl downTo 1) duration += (time[dl - i].ifEmpty { "0" }).toLong() * 60.0.pow(
                                            ((i - 1).toDouble())
                                        ).toLong()
                                    } catch (exception: Exception) {
                                        duration = 1440
                                        JLogger.warning("Error on get time $anime episode: ${exception.message}")
                                    } finally {
                                        driverEpisode.quit()
                                    }

                                    this.episodes[id] = duration
                                } else duration = this.episodes[id]!!
                            } else duration = 1440

                            val episode = Episode(
                                platform = this.getName(),
                                calendar = ISO8601.fromCalendar(releaseDate),
                                anime = anime.toString(),
                                number = number,
                                country = country,
                                type = type,
                                id = id,
                                title = null,
                                image = image,
                                link = link,
                                duration = duration
                            )
                            episode.p = this
                            l.add(episode)
                        }
                    }
                }
            } catch (exception: Exception) {
                JLogger.warning("Error on get Wakanim episode: ${exception.message}")
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
}