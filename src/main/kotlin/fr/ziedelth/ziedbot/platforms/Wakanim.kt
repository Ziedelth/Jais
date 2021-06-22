package fr.ziedelth.ziedbot.platforms

import fr.ziedelth.ziedbot.utils.animes.*
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.awt.Color
import java.util.*

class Wakanim : Platform {
    init {
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true")
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null")
    }

    override fun getName(): String = "Wakanim"
    override fun getURL(): String = "https://www.wakanim.tv/"
    override fun getImage(): String =
        "https://play-lh.googleusercontent.com/J5_U63e4nJPrSUHeqqGIoZIaqQ1EYKEeXpcNaVbf95adUu9O9VnEgXC_ejUZPaCjpw"

    override fun getColor(): Color = Color(227, 71, 75)
    override fun getAllowedLanguages(): Array<Language> = arrayOf(Language.FRENCH)

    override fun getLastNews(): Array<News> = arrayListOf<News>().toTypedArray()

    override fun getLastEpisodes(): Array<Episode> {
        val l: MutableList<Episode> = mutableListOf()
        val calendar = Calendar.getInstance()

        Language.values().filter { this.getAllowedLanguages().contains(it) }.forEach { language ->
            val options = FirefoxOptions()
            options.setHeadless(true)
            val driver = FirefoxDriver(options)
            val wait = WebDriverWait(driver, 60)

            try {
                driver.get("https://www.wakanim.tv/${language.country}/v2/agenda")

                val cookiesButton = aS(wait, "css-1fxzzmg")
                cookiesButton?.click()

                val privacyClose = aS(wait, "privacy-close")
                privacyClose?.click()

                val premiumEpisodes = aS(wait, "Calendar-newEp")
                premiumEpisodes?.click()

                val list = aM(wait, "Calendar-today", "Calendar-ep")
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
                                val number = aPS(wait, linkElement, "Calendar-epNumber")!!.text
                                val ltype = aPS(wait, it, "Calendar-tagTranslation")!!.text

                                val episodeType =
                                    if (ltype.equals(language.voice, true)) EpisodeType.VOICE else EpisodeType.SUBTITLES
                                val id = link.replace("//", "/").split("/")[6]

                                val episode = Episode(
                                    this.getName(),
                                    toStringCalendar(releaseDate),
                                    anime,
                                    id,
                                    null,
                                    image,
                                    link,
                                    number,
                                    language,
                                    episodeType
                                )
                                episode.p = this
                                l.add(episode)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                return getLastEpisodes()
            } finally {
                driver.quit()
            }
        }

        return l.toTypedArray()
    }

    private fun setDate(hour: Int, minutes: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
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

    private fun aM(wait: WebDriverWait, parent: String, sClass: String): List<WebElement>? {
        return wait.until(
            ExpectedConditions.presenceOfNestedElementsLocatedBy(
                By.className(parent),
                By.className(sClass)
            )
        )
    }
}