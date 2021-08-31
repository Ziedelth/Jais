/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.*
import org.jsoup.Jsoup
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.Color
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class Crunchyroll : Platform {
    override fun getName(): String = "Crunchyroll"
    override fun getURL(): String = "https://www.crunchyroll.com/"
    override fun getImage(): String = "https://ziedelth.fr/images/crunchyroll.png"
    override fun getColor(): Color = Color(255, 108, 0)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    override fun getLastNews(): Array<News> {
        val calendar = Calendar.getInstance()
        val l: MutableList<News> = mutableListOf()

        this.getAllowedCountries().forEach { country ->
            val url: URLConnection
            val list: NodeList

            try {
                url = URL("${this.getURL()}newsrss?lang=${country.lang}").openConnection()
                list = Const.getItems(url, "item")
            } catch (exception: Exception) {
                return l.toTypedArray()
            }

            for (i in 0 until list.length) {
                val node = list.item(i)

                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element

                    val releaseDate = toCalendar(element.getElementsByTagName("pubDate").item(0).textContent)

                    if (Const.isSameDay(calendar, releaseDate) && calendar.after(releaseDate)) continue

                    val title = element.getElementsByTagName("title").item(0).textContent
                    val description =
                        Jsoup.parse(element.getElementsByTagName("description").item(0).textContent).text()
                    val content =
                        Jsoup.parse(element.getElementsByTagName("content:encoded").item(0).textContent).text()
                    val link = element.getElementsByTagName("guid").item(0).textContent

                    val news =
                        News(
                            this.getName(),
                            ISO8601.fromCalendar(releaseDate),
                            title,
                            description,
                            content,
                            link,
                            country
                        )
                    news.p = this
                    l.add(news)
                }
            }
        }

        return l.toTypedArray()
    }

    override fun getLastEpisodes(): Array<Episode> {
        val calendar = Calendar.getInstance()
        val l: MutableList<Episode> = mutableListOf()

        this.getAllowedCountries().forEach { country ->
            val url: URLConnection
            val list: NodeList

            try {
                url = URL("${this.getURL()}rss/anime?lang=${country.lang}").openConnection()
                list = Const.getItems(url, "item")
            } catch (exception: Exception) {
                return l.toTypedArray()
            }

            for (i in 0 until list.length) {
                val node = list.item(i)

                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element

                    val date = element.getElementsByTagName("pubDate").item(0)?.textContent
                    if (date.isNullOrEmpty()) continue
                    val releaseDate = toCalendar(date)
                    if (!(Const.isSameDay(calendar, releaseDate) && calendar.after(releaseDate))) continue

                    val spay = element.getElementsByTagName("media:restriction").item(0).textContent
                    if (!spay.split(" ").contains(country.country)) continue
                    val s: String? = element.getElementsByTagName("crunchyroll:season")?.item(0)?.textContent

                    val season: String = if (s.isNullOrEmpty() || s.equals("null", true)) {
                        "1"
                    } else {
                        Const.toInt(s, "1")
                    }

                    val anime = element.getElementsByTagName("crunchyroll:seriesTitle").item(0).textContent
                    var title: String? = element.getElementsByTagName("crunchyroll:episodeTitle").item(0)?.textContent
                    if (title.isNullOrEmpty()) title = null
                    val image =
                        (element.getElementsByTagName("media:thumbnail").item(0) as Element?)?.getAttribute("url")
                            ?.replace(" ", "%20") ?: ""
                    val link = element.getElementsByTagName("guid").item(0).textContent.replace(" ", "%20")
                    val number = Const.toInt(
                        element.getElementsByTagName("crunchyroll:episodeNumber").item(0)?.textContent ?: ""
                    )
                    if (number.isEmpty()) continue
                    val subtitles =
                        element.getElementsByTagName("crunchyroll:subtitleLanguages").item(0)?.textContent ?: ""
                    val type =
                        if (subtitles.equals(country.language, true)) EpisodeType.DUBBED else EpisodeType.SUBTITLED
                    val id = element.getElementsByTagName("crunchyroll:mediaId").item(0).textContent.toLong()
                    val duration = element.getElementsByTagName("crunchyroll:duration").item(0)?.textContent ?: "1440"

                    if (subtitles.split(",")
                            .contains(country.language)
                    ) {
                        l.add(
                            Episode(
                                platform = this,
                                calendar = ISO8601.fromCalendar(releaseDate),
                                anime = anime,
                                number = number,
                                country = country,
                                type = type,
                                season = season,
                                episodeId = id,
                                title = title,
                                image = image,
                                url = link,
                                duration = duration.toLong()
                            )
                        )
                    }
                }
            }
        }

        return l.toTypedArray()
    }

    private fun toCalendar(s: String): Calendar {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH).parse(s)
        calendar.time = date
        return calendar
    }
}