/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.Impl.toHTTPS
import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.Scan
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.InputStreamReader
import java.net.URL
import java.util.*

@PlatformHandler(
    name = "Japscan",
    url = "https://www.japscan.ws/",
    image = "images/platforms/japscan.png",
    color = 0xF05A28,
    countries = [FranceCountry::class]
)
class JapscanPlatform(jais: Jais) : Platform(jais) {
    data class Japscan(val anime: String?, val image: String?, val genres: Array<Genre>?, val description: String?)

    private val japscan: MutableList<Japscan> = mutableListOf()

    @Synchronized
    override fun checkScans(calendar: Calendar): Array<Scan> {
        val pairPlatformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Scan>()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { pairCountryImpl ->
            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} scan(s):") {
                val urlConnection = URL("${pairPlatformImpl.first.url}rss/").openConnection()
                urlConnection.connectTimeout = 10000
                urlConnection.readTimeout = 10000
                val inputStream = urlConnection.getInputStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()

                Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.mapNotNull { Impl.toObject(it) }
                    ?.forEachIndexed { _, scanObject ->
                        val titleNS = Impl.getString(scanObject, "title") ?: return@forEachIndexed
                        if (this.checkedEpisodes.contains(titleNS)) return@forEachIndexed
                        val releaseDate =
                            ISO8601.fromUTCDate(ISO8601.fromCalendar2(Impl.getString(scanObject, "pubDate")))
                                ?: return@forEachIndexed
                        if (!ISO8601.isSameDayUsingInstant(
                                calendar,
                                releaseDate
                            ) || calendar.before(releaseDate)
                        ) return@forEachIndexed

                        val split = titleNS.split(" ")
                        val anime = split.subList(0, split.size - 2).joinToString(" ")
                        val string = Impl.getString(scanObject, "link")
                        val link = pairPlatformImpl.first.url + string?.subSequence(1 until string.length)
                        val number = split[split.size - 2].toLongOrNull() ?: return@forEachIndexed
                        val animeLink =
                            pairPlatformImpl.first.url + "manga/" + (string?.split("/")
                                ?.get(2) ?: return@forEachIndexed) + "/"

                        if (!this.japscan.any { it.anime.equals(anime, true) }) {
                            val document = JBrowser.get(animeLink)
                            val localLink =
                                document?.selectXpath("/html/body/div[1]/div/div[1]/div[1]/div/div[2]/div[1]/img")
                                    ?.attr("src")?.toHTTPS()
                            val animeImage =
                                pairPlatformImpl.first.url + (localLink?.subSequence(1 until localLink.length)
                                    ?: return@forEachIndexed)
                            JLogger.info("Found new anime: $anime")
                            JLogger.info("Anime image: $animeImage")
                            // /html/body/div[1]/div/div[1]/div[1]/div/div[2]/div[2]/p
                            val animeGenres = Genre.getGenres(
                                document.selectXpath("/html/body/div[1]/div/div[1]/div[1]/div/div[2]/div[2]/p")
                                    .firstOrNull { it.text().startsWith("Genre(s): ") }
                                    ?.text()?.split("Genre(s): ")?.lastOrNull()?.split(", ")
                            )
                            JLogger.info("Anime genres: ${animeGenres.joinToString { it.name }}")
                            val animeDescription =
                                document.selectXpath("/html/body/div[1]/div/div[1]/div[1]/div/p").text()
                            JLogger.info("Anime description: $animeDescription")
                            this.japscan.add(Japscan(anime, animeImage, animeGenres, animeDescription))
                        }

                        val japscan = this.japscan.find { it.anime.equals(anime, true) } ?: return@forEachIndexed
                        val animeImage = japscan.image
                        val animeGenres = japscan.genres ?: emptyArray()
                        val animeDescription = japscan.description

                        this.addCheck(titleNS)
                        list.add(
                            Scan(
                                pairPlatformImpl,
                                pairCountryImpl,
                                releaseDate,
                                anime,
                                animeImage,
                                animeGenres,
                                animeDescription,
                                number,
                                url = link
                            )
                        )
                    }
            }
        }

        return list.toTypedArray()
    }
}