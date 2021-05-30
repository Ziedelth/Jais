package fr.ziedelth.ziedbot.platforms

import fr.ziedelth.ziedbot.utils.Const
import fr.ziedelth.ziedbot.utils.animes.Episode
import fr.ziedelth.ziedbot.utils.animes.News
import fr.ziedelth.ziedbot.utils.animes.Platform
import org.jsoup.Jsoup
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.awt.Color
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class Crunchyroll : Platform {
    override fun getName(): String = "Crunchyroll"
    override fun getURL(): String = "https://www.crunchyroll.com/"
    override fun getImage(): String =
        "https://archive.org/download/crunchyroll.-1.1.0/Crunchyroll.1.1.0/ico_android_settings.png"

    override fun getColor(): Color = Color(255, 108, 0)

    override fun getLastNews(): MutableList<News> {
        val calendar = Const.CHECK_DATE.getDate()
        val l: MutableList<News> = mutableListOf()
        val url: URLConnection

        try {
            url = URL("https://www.crunchyroll.com/newsrss?lang=frFR").openConnection()
        } catch (exception: Exception) {
            return l
        }

        val dbf = DocumentBuilderFactory.newInstance()
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(url.getInputStream())
        doc.documentElement.normalize()
        val list = doc.getElementsByTagName("item")

        for (i in 0 until list.length) {
            val node = list.item(i)

            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val title = element.getElementsByTagName("title").item(0).textContent
                val description = Jsoup.parse(element.getElementsByTagName("description").item(0).textContent).text()
                val content = Jsoup.parse(element.getElementsByTagName("content:encoded").item(0).textContent).text()
                val date = element.getElementsByTagName("pubDate").item(0).textContent
                val releaseDate = toCalendar(date)
                val link = element.getElementsByTagName("guid").item(0).textContent

                if (this.isSameDay(calendar, releaseDate)) {
                    val news = News(
                        this.getName(),
                        toStringCalendar(releaseDate),
                        title,
                        description,
                        content,
                        link
                    )

                    news.p = this
                    l.add(news)
                }
            }
        }

        return l
    }

    override fun getLastEpisodes(): MutableList<Episode> {
        val l: MutableList<Episode> = mutableListOf()
        val url: URLConnection

        try {
            url = URL("https://feeds.feedburner.com/crunchyroll/rss/anime?lang=frFR").openConnection()
        } catch (exception: Exception) {
            return l
        }

        val dbf = DocumentBuilderFactory.newInstance()
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(url.getInputStream())
        doc.documentElement.normalize()
        val list = doc.getElementsByTagName("item")
        val calendar = Calendar.getInstance()

        for (i in 0 until list.length) {
            val node = list.item(i)

            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val anime = element.getElementsByTagName("crunchyroll:seriesTitle").item(0).textContent
                val title: String? = element.getElementsByTagName("crunchyroll:episodeTitle").item(0)?.textContent
                val subtitles = element.getElementsByTagName("crunchyroll:subtitleLanguages").item(0)?.textContent ?: ""
                val image = (element.getElementsByTagName("media:thumbnail").item(0) as Element).getAttribute("url")
                    .replace(" ", "%20")
                val date = element.getElementsByTagName("pubDate").item(0).textContent
                val link = element.getElementsByTagName("guid").item(0).textContent.replace(" ", "%20")
                val number = element.getElementsByTagName("crunchyroll:episodeNumber").item(0)?.textContent
                val spay = element.getElementsByTagName("media:restriction").item(0).textContent
                val releaseDate = toCalendar(date)

                if (spay.split(" ").contains("fr") && subtitles.split(",").contains("fr - fr") && this.isSameDay(
                        calendar,
                        releaseDate
                    )
                ) {
                    val episode = Episode(this.getName(), toStringCalendar(releaseDate), title, image, link, "$number")
                    episode.p = this
                    episode.anime = anime
                    l.add(episode)
                }
            }
        }

        return l
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH).parse(s)
        calendar.time = date
        return calendar
    }

    private fun isSameDay(var0: Calendar, var1: Calendar): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd")
        return fmt.format(var0.time) == fmt.format(var1.time)
    }
}