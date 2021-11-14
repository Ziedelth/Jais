/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.WebDriverBuilder
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.platforms.CrunchyrollEpisode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
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
            var webDriverImpl: WebDriverBuilder.WebDriverImpl? = null

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
                episodesList?.forEach { it.platform = this; it.country = country }

                episodesList?.filter {
                    !this.checkedEpisodes.contains(it.mediaId) && it.isValid() && ISO8601.isSameDayUsingISO8601(
                        ISO8601.fromCalendar2(it.pubDate),
                        ISO8601.fromCalendar(calendar)
                    ) && calendar.after(ISO8601.toCalendar2(it.pubDate))
                }?.sortedBy { ISO8601.toCalendar2(it.pubDate) }?.forEachIndexed { _, crunchyrollEpisode ->
                    if (!this.animes.any { it.anime.equals(crunchyrollEpisode.seriesTitle, true) }) {
                        if (webDriverImpl == null) webDriverImpl = WebDriverBuilder.setDriver()
                        webDriverImpl?.driver?.get(crunchyrollEpisode.link)
                        Impl.tryCatch {
                            webDriverImpl?.driver?.findElement(By.xpath("//*[@id=\"onetrust-accept-btn-handler\"]"))
                                ?.click().let { webDriverImpl?.driver?.get(crunchyrollEpisode.link) }
                        }
                        val animeUrl =
                            webDriverImpl?.wait?.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"showmedia_about_episode_num\"]/a")))
                                ?.getAttribute("href")

                        webDriverImpl?.driver?.get(animeUrl)
                        Impl.tryCatch {
                            webDriverImpl?.driver?.findElement(By.xpath("//*[@id=\"onetrust-accept-btn-handler\"]"))
                                ?.click().let { webDriverImpl?.driver?.get(animeUrl) }
                        }

                        val animeImage =
                            webDriverImpl?.wait?.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"sidebar_elements\"]/li[1]/img")))
                                ?.getAttribute("src")
                        val animeDescriptionElement =
                            webDriverImpl?.wait?.until(ExpectedConditions.presenceOfElementLocated(By.className("description")))
                        webDriverImpl?.wait?.until(
                            ExpectedConditions.presenceOfNestedElementLocatedBy(
                                animeDescriptionElement,
                                By.tagName("a")
                            )
                        )?.click()
                        val animeDescription = webDriverImpl?.wait?.until(
                            ExpectedConditions.presenceOfNestedElementLocatedBy(
                                animeDescriptionElement,
                                By.className("more")
                            )
                        )?.text

                        crunchyrollEpisode.seriesImage = animeImage
                        crunchyrollEpisode.description = animeDescription

                        this.animes.add(CrunchyrollAnime(crunchyrollEpisode.seriesTitle, animeImage, animeDescription))
                    } else {
                        val crunchyrollAnime =
                            this.animes.find { it.anime.equals(crunchyrollEpisode.seriesTitle, true) }!!
                        crunchyrollEpisode.seriesImage = crunchyrollAnime.image
                        crunchyrollEpisode.description = crunchyrollAnime.description
                    }

                    val episode = crunchyrollEpisode.toEpisode() ?: return@forEachIndexed

                    list.add(episode)
                    this.addCheckEpisodes(crunchyrollEpisode.mediaId!!)
                }
            }

            webDriverImpl?.driver?.quit()
        }

        return list.toTypedArray()
    }
}