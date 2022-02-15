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
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.Scan
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import org.jsoup.Jsoup
import java.io.InputStreamReader
import java.net.URL
import java.util.*

@PlatformHandler(
    name = "Scantrad",
    url = "https://scantrad.net/",
    image = "images/platforms/scantrad.jpg",
    color = 0xF05A28,
    countries = [FranceCountry::class]
)
class ScantradPlatform(jais: Jais) : Platform(jais) {
    data class Scantrad(val anime: String?, val image: String?, val genres: Array<Genre>?, val description: String?)

    private val scantrad: MutableList<Scantrad> = mutableListOf()

    @Synchronized
    override fun checkScans(calendar: Calendar): Array<Scan> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Scan>()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = this.jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val urlConnection = URL("https://scantrad.net/rss/").openConnection()
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

                        val titleSplitter = titleNS.split("Scan - ")[1].split(" ")
                        val descriptionObject = Impl.getString(scanObject, "description")
                        val descriptionDocument = Jsoup.parse(descriptionObject ?: "")
                        val animeLink = descriptionDocument.getElementsByTag("a").attr("href").toHTTPS()

                        val anime = titleSplitter.subList(0, titleSplitter.size - 2).joinToString(" ")
                        val number = titleSplitter.lastOrNull()?.toLongOrNull() ?: return@forEachIndexed
                        val url = Impl.getString(scanObject, "link")?.toHTTPS() ?: return@forEachIndexed

                        if (!this.scantrad.any { it.anime.equals(anime, true) }) {
                            val document = JBrowser.get(animeLink)
                            val animeImage =
                                document?.selectXpath("//*[@id=\"chap-top\"]/div[1]/div[1]/img")?.attr("src")?.toHTTPS()
                            val animeGenres = Genre.getGenres(
                                document?.selectXpath("//*[@id=\"chap-top\"]/div[1]/div[2]/div[2]/div[2]")
                                    ?.firstOrNull()?.getElementsByClass("snm-button")?.map { it.text() })
                            val animeDescription =
                                document?.getElementsByClass("new-main")?.firstOrNull()?.getElementsByTag("p")?.text()
                            this.scantrad.add(Scantrad(anime, animeImage, animeGenres, animeDescription))
                        }

                        val scantrad = this.scantrad.find { it.anime.equals(anime, true) } ?: return@forEachIndexed
                        val animeImage = scantrad.image
                        val animeGenres = scantrad.genres ?: emptyArray()
                        val animeDescription = scantrad.description

                        this.addCheck(titleNS)
                        list.add(
                            Scan(
                                platformImpl,
                                countryImpl,
                                releaseDate,
                                anime,
                                animeImage,
                                animeGenres,
                                animeDescription,
                                number,
                                url = url
                            )
                        )
                    }
            }
        }

        return list.toTypedArray()
    }
}