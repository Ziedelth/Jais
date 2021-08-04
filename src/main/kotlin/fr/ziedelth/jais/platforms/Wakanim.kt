package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.DriverBuilder
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.*
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.remote.ProtocolHandshake
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.set
import kotlin.math.pow

class Wakanim : Platform {
    private val timeout = 60L
    private val options: FirefoxOptions = FirefoxOptions().setHeadless(true).setProfile(FirefoxProfile())
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
            val driverWait = WebDriverWait(driver, this.timeout)

            try {
                driver.get("${this.getURL()}${country.country}/v2/agenda/getevents?s=$date&e=$date&free=false")
                DriverBuilder.addDriver(driver)

                val episodeList =
                    driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("/html/body/div[1]/div[2]/div")))

                if (!(episodeList.size == 1 && episodeList[0].text == "Pas de nouveaux épisodes !")) {
                    val list: MutableList<EpisodeBuilder> = mutableListOf()

                    episodeList.forEachIndexed { index, webElement ->
                        val episodeBuilder = EpisodeBuilder()
                        val fullText = webElement.text.replace("\n", " ")
                        val splitFullText = fullText.split(" ")

                        val releaseDate = setDate(
                            date,
                            splitFullText[0].split(":")[0].toInt(),
                            splitFullText[0].split(":")[1].toInt()
                        )
                        episodeBuilder.calendar = releaseDate

                        val linkElement =
                            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[${(index + 1)}]/div[2]/a")))

                        val link = linkElement.getAttribute("href")
                        episodeBuilder.link = link

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

                        val image =
                            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[${(index + 1)}]/div[2]/a/img")))
                                .getAttribute("src")
                        episodeBuilder.image = image

                        val id = link.replace("//", "/").split("/")[6]
                        episodeBuilder.id = id
                        list.add(episodeBuilder)
                    }

                    list.forEach {
                        val tt = it.link?.replace("//", "/")?.split("/")?.get(5)
                        var duration: Long = 0

                        if (calendar.after(it.calendar)) {
                            if (tt.equals("episode", true)) {
                                if (!this.episodes.containsKey(it.id)) {
                                    driver.get(it.link)
                                    val time = driverWait.until(
                                        ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                                            By.className("currentEp"), By.className("slider_item_duration")
                                        )
                                    )[0].text.split(":")

                                    val dl = time.size
                                    for (i in dl downTo 1) duration += (time[dl - i].ifEmpty { "0" }).toLong() * 60.0.pow(
                                        ((i - 1).toDouble())
                                    ).toLong()

                                    if (duration > 0L) this.episodes[it.id!!] = duration
                                } else duration = this.episodes[it.id]!!
                            } else duration = 1440

                            it.duration = duration

                            val episode = Episode(
                                platform = this.getName(),
                                calendar = ISO8601.fromCalendar(it.calendar!!),
                                anime = it.anime!!,
                                number = it.number!!,
                                country = country,
                                type = it.type!!,
                                id = it.id!!,
                                title = null,
                                image = it.image!!,
                                link = it.link!!,
                                duration = it.duration
                            )
                            episode.p = this
                            l.add(episode)
                        }
                    }
                }
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Error on get Wakanim episode", exception)
            } finally {
                DriverBuilder.removeDriver(driver)
                driver.quit()
            }
        }

        return l.toTypedArray()
    }

    private fun date(calendar: Calendar): String {
        return SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    }

    private fun setDate(date: String, hour: Int, minutes: Int): Calendar {
        val calendar = Calendar.getInstance()
        val d = SimpleDateFormat("dd-MM-yyyy").parse(date)
        calendar.time = d

        calendar.set(Calendar.HOUR_OF_DAY, hour + 2)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }
}