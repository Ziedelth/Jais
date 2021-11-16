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
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.platforms.CrunchyrollEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.InputStreamReader
import java.net.URL
import java.util.*

@PlatformHandler(
    name = "Crunchyroll",
    url = "https://www.crunchyroll.com/",
    image = "images/platforms/crunchyroll.jpg",
    color = 0xFF6C00,
    countries = [FranceCountry::class]
)
class CrunchyrollPlatform : Platform() {
    data class CrunchyrollAnime(
        val anime: String?,
        val image: String?,
        val description: String?
    )

    private val animes: MutableList<CrunchyrollAnime> = emptyArray<CrunchyrollAnime>().toMutableList()

    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val list = mutableListOf<Episode>()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val xmlMapper = XmlMapper()
        val objectMapper = ObjectMapper()

        this.getAllowedCountries().forEach { country ->
            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                val inputStream =
                    URL("https://www.crunchyroll.com/rss/anime?lang=${country.checkOnEpisodesURL(this)}").openStream()
                val jsonObject: JsonObject? = gson.fromJson(
                    objectMapper.writeValueAsString(xmlMapper.readTree(InputStreamReader(inputStream))),
                    JsonObject::class.java
                )
                inputStream.close()
                val episodesList =
                    (jsonObject?.get("channel") as JsonObject?)?.get("item")?.asJsonArray?.filter { it != null && it.isJsonObject }
                        ?.mapNotNull { gson.fromJson(it, CrunchyrollEpisode::class.java) }

                episodesList?.forEach {
                    it.platformImpl = Jais.getPlatformInformation(this)
                    it.countryImpl = Jais.getCountryInformation(country)
                }

                episodesList?.filter {
                    !this.checkedEpisodes.contains(it.mediaId) && it.isValid() && ISO8601.isSameDayUsingISO8601(
                        ISO8601.fromCalendar2(it.pubDate),
                        ISO8601.fromCalendar(calendar)
                    ) && calendar.after(ISO8601.toCalendar2(it.pubDate))
                }?.forEachIndexed { _, crunchyrollEpisode ->
                    if (!this.animes.any { it.anime.equals(crunchyrollEpisode.seriesTitle, true) }) {
                        var result = JBrowser.get(crunchyrollEpisode.link)
                        val animeUrl = result?.selectXpath("//*[@id=\"showmedia_about_episode_num\"]/a")?.attr("href")
                        result = JBrowser.get(animeUrl)
                        val animeImage = result?.selectXpath("//*[@id=\"sidebar_elements\"]/li[1]/img")?.attr("src")

                        var animeDescription = result?.getElementsByClass("more")?.text()
                        if (animeDescription.isNullOrBlank()) animeDescription =
                            result?.getElementsByClass("trunc-desc")?.text()

                        crunchyrollEpisode.seriesImage = animeImage
                        crunchyrollEpisode.description = animeDescription

                        this.animes.add(CrunchyrollAnime(crunchyrollEpisode.seriesTitle, animeImage, animeDescription))
                    } else {
                        val crunchyrollAnime =
                            this.animes.find { it.anime.equals(crunchyrollEpisode.seriesTitle, true) }!!
                        crunchyrollEpisode.seriesImage = crunchyrollAnime.image
                        crunchyrollEpisode.description = crunchyrollAnime.description
                    }

                    if (crunchyrollEpisode.seriesImage.isNullOrBlank()) return@forEachIndexed

                    val episode = crunchyrollEpisode.toEpisode() ?: return@forEachIndexed

                    list.add(episode)
                    this.addCheckEpisodes(crunchyrollEpisode.mediaId!!)
                }
            }
        }

        return list.toTypedArray()
    }
}