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
class ScantradPlatform : Platform() {
    @Synchronized
    override fun checkScans(calendar: Calendar): Array<Scan> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Scan>()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = Jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val inputStream = URL("https://scantrad.net/rss/").openStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )

                Impl.getArray(Impl.getObject(jsonObject, "channel"), "item")?.mapNotNull { Impl.toObject(it) }
                    ?.forEachIndexed { _, scanObject ->
                        val titleNS = Impl.getString(scanObject, "title") ?: return@forEachIndexed
                        if (this.checkedEpisodes.contains(titleNS)) return@forEachIndexed
                        val releaseDate =
                            ISO8601.fromUTCDate(ISO8601.fromCalendar4(Impl.getString(scanObject, "pubDate")))
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

                        val document = JBrowser.get(animeLink)
                        val animeImage =
                            document?.selectXpath("//*[@id=\"chap-top\"]/div[1]/div[1]/img")?.attr("src")?.toHTTPS()
                                ?: return@forEachIndexed
                        val animeGenres = Genre.getGenres(
                            document.selectXpath("//*[@id=\"chap-top\"]/div[1]/div[2]/div[2]/div[2]").firstOrNull()
                                ?.getElementsByClass("snm-button")?.map { it.text() })
                        val animeDescription =
                            document.getElementsByClass("new-main").firstOrNull()?.getElementsByTag("p")?.text()

                        this.addCheckEpisodes(titleNS)
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