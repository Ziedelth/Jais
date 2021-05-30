package fr.ziedelth.ziedbot.platforms

import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.Request
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.News
import fr.ziedelth.ziedbot.utils.animes.Platform
import org.jsoup.Jsoup
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class Wakanim : Platform {
    override fun getName(): String = "Wakanim"
    override fun getURL(): String = "https://www.wakanim.tv/"
    override fun getImage(): String =
        "https://play-lh.googleusercontent.com/J5_U63e4nJPrSUHeqqGIoZIaqQ1EYKEeXpcNaVbf95adUu9O9VnEgXC_ejUZPaCjpw"

    override fun getColor(): Color = Color(227, 71, 75)

    override fun getLastNews(): MutableList<News> = mutableListOf()

    override fun getLastEpisodes(): MutableList<Episode> {
        val calendar = Const.CHECK_DATE.getDate()
        val l: MutableList<Episode> = mutableListOf()
        val response: String

        try {
            response = Request.get(
                "https://www.wakanim.tv/fr/v2/agenda/getevents?s=${this.date(calendar)}&e=${
                    this.date(calendar)
                }&free=false"
            )
        } catch (exception: Exception) {
            return l
        }

        val doc = Jsoup.parseBodyFragment(response)
        val episodes = doc.getElementsByClass("Calendar-ep")

        episodes.forEach {
            val anime = it.getElementsByClass("Calendar-epTitle")[0].text()
            val image = "https:" + it.getElementsByClass("Calendar-image")[0].attr("src")
            val releaseDate = toCalendar(it.getElementsByClass("Calendar-hourTxt")[0].text())
            val link = "https://www.wakanim.tv" + it.getElementsByClass("Calendar-numero")[0].attr("href")
            val number = it.getElementsByClass("Calendar-epNumber")[0].text().toInt()

            if (calendar.after(releaseDate)) {
                val episode = Episode(this.getName(), toStringCalendar(releaseDate), null, image, link, "$number")
                episode.p = this
                episode.anime = anime
                l.add(episode)
            }
        }

        return l
    }

    private fun date(calendar: Calendar): String {
        return SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, s.split(":")[0].toInt() + 2)
        calendar.set(Calendar.MINUTE, s.split(":")[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }
}