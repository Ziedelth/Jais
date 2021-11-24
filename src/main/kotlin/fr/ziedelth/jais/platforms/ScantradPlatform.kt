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
import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.animes.AnimeGenre
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.scans.Scan
import fr.ziedelth.jais.utils.animes.scans.platforms.ScantradScan
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
        val list = mutableListOf<Scan>()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()
        val platformImpl = Jais.getPlatformInformation(this)

        this.getAllowedCountries().forEach { country ->
            val countryImpl = Jais.getCountryInformation(country)

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val inputStream =
                    URL("https://scantrad.net/rss/").openStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()
                val scansList =
                    (jsonObject?.get("channel") as JsonObject?)?.get("item")?.asJsonArray?.filter { it != null && it.isJsonObject }
                        ?.mapNotNull { gson.fromJson(it, ScantradScan::class.java) }

                scansList?.forEach {
                    it.platformImpl = platformImpl
                    it.countryImpl = countryImpl
                }

                scansList?.filter {
                    !this.checkedEpisodes.contains("${it.title}") && it.isValid() && ISO8601.isSameDayUsingISO8601(
                        ISO8601.fromCalendar2(it.pubDate),
                        ISO8601.fromCalendar(calendar)
                    ) && calendar.after(ISO8601.toCalendar2(it.pubDate))
                }?.forEachIndexed { _, scantradScan ->
                    val descriptionDocument = Jsoup.parse(scantradScan.description ?: "")
                    val animeLink = descriptionDocument.getElementsByTag("a").attr("href")

                    val document = JBrowser.get(animeLink)
                    val animeGenre = AnimeGenre.getGenres(
                        document?.selectXpath("//*[@id=\"chap-top\"]/div[1]/div[2]/div[2]/div[2]")?.firstOrNull()
                            ?.getElementsByClass("snm-button")?.map { it.text() }?.toTypedArray()
                    )
                    val animeDescription =
                        document?.getElementsByClass("new-main")?.firstOrNull()?.getElementsByTag("p")?.text()

                    scantradScan.genres = animeGenre
                    scantradScan.animeDescription = animeDescription

                    val scan = scantradScan.toScan() ?: return@forEachIndexed

                    list.add(scan)
                    this.addCheckEpisodes("${scantradScan.title}")
                }
            }
        }

        return list.toTypedArray()
    }
}