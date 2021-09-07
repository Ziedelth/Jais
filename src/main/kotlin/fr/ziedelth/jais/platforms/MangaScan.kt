/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.utils.Const
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Country
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Platform
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.Color
import java.net.URL
import java.net.URLConnection
import java.util.*
import java.util.logging.Level

class MangaScan : Platform {
    override fun getName(): String = "MangaScan"
    override fun getURL(): String = "https://mangascan.cc/"
    override fun getImage(): String = "https://ziedelth.fr/images/mangascan.png"
    override fun getColor(): Color = Color(191, 62, 17)
    override fun getAllowedCountries(): Array<Country> = arrayOf(Country.FRANCE)

    override fun getLastEpisodes(): Array<Episode> {
        val calendar = Calendar.getInstance()
        val l: MutableList<Episode> = mutableListOf()

        this.getAllowedCountries().forEach { country ->
            val url: URLConnection
            val list: NodeList

            try {
                url = URL("${this.getURL()}feed").openConnection()
                list = Const.getItems(url, "entry")
            } catch (exception: Exception) {
                JLogger.log(Level.WARNING, "Can not get episodes on ${this.getName()}", exception)
                return l.toTypedArray()
            }

            for (i in 0 until list.length) {
                val node = list.item(i)

                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element

                    val updated = element.getElementsByTagName("updated").item(0)?.textContent
                    if (updated.isNullOrEmpty()) continue
                    val releaseDate = ISO8601.toCalendar(updated)

                    if (!(Const.isSameDay(calendar, releaseDate) && calendar.after(releaseDate))) continue
                    val a = element.getElementsByTagName("title").item(0).textContent
                    var anime: String
                    var image: String

                    if (a.contains("Solo Leveling", true)) {
                        anime = "Solo Leveling"
                        image = "https://45secondes.fr/wp-content/uploads/2021/07/Solo-Leveling-Chapitre-157.jpg"
                    } else if (a.contains("My Hero Academia", true)) {
                        anime = "My Hero Academia"
                        image = "https://i.ytimg.com/vi/3BmYAeZJnQU/maxresdefault.jpg"
                    } else if (a.contains("One Piece", true)) {
                        anime = "One Piece"
                        image = "https://i.pinimg.com/originals/d1/00/92/d1009254c0370706696faad1800154aa.jpg"
                    } else if (a.contains("Jujutsu Kaisen", true)) {
                        anime = "Jujutsu Kaisen"
                        image = "https://pbs.twimg.com/media/Ei7nh5_X0AAvuav.jpg"
                    } else if (a.contains("Eden’s Zero", true)) {
                        anime = "Eden’s Zero"
                        image = "https://www.fun-academy.fr/wp-content/uploads/2021/06/edens-zero-manga.jpeg"
                    } else continue

                    val link = (element.getElementsByTagName("link").item(0) as Element?)?.getAttribute("href")
                    val number = a.split("#").lastOrNull()?.replace(" ", "")
                    if (number.isNullOrEmpty()) continue
                    val b = element.getElementsByTagName("summary").item(0).textContent
                    if (b.contains("(VA)", true)) continue

                    l.add(
                        Episode(
                            platform = this,
                            calendar = ISO8601.fromCalendar(releaseDate),
                            anime = anime,
                            number = number,
                            country = country,
                            type = EpisodeType.DUBBED,
                            season = "1",
                            episodeId = 0,
                            title = null,
                            image = image,
                            url = link,
                            duration = 0
                        )
                    )
                }
            }
        }

        return l.toTypedArray()
    }


}