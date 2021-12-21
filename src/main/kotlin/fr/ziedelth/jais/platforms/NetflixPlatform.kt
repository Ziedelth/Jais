/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.Impl.toHTTPS
import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.text.SimpleDateFormat
import java.util.*

@PlatformHandler(
    name = "Netflix",
    url = "https://netflix.com/",
    image = "images/platforms/netflix.png",
    color = 0xE50914,
    countries = [FranceCountry::class]
)
class NetflixPlatform : Platform() {
    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = Jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                if (calendar.get(Calendar.DAY_OF_WEEK) == 5) {
                    // Komi can't communicate
                    val id = 81228573
                    val url = "https://www.netflix.com/${country.checkOnEpisodesURL(this)}/title/$id"
                    val releaseDate = ISO8601.fromUTCDate("${getISODate(calendar)}T08:00:00Z")

                    if (!this.checkedEpisodes.contains(id.toString()) && ISO8601.isSameDayUsingInstant(
                            calendar,
                            releaseDate
                        ) && calendar.after(releaseDate)
                    ) {
                        val document = JBrowser.get(url)
                        // val style = document?.selectXpath("//*[@id=\"section-hero\"]/div[1]/div[2]/div[1]")?.attr("style")
                        val latestEpisode = document?.getElementsByClass("episode")?.maxByOrNull {
                            it?.getElementsByClass("episode-title")?.text()?.split(". ")?.get(0)?.toLongOrNull() ?: -1
                        }
                        val fet = latestEpisode?.getElementsByClass("episode-title")?.text()

                        val anime =
                            document?.selectXpath("//*[@id=\"section-seasons-and-episodes\"]/div[1]/h2[2]")?.text()
                                ?: return@tryCatch
                        // val animeImage = style?.split("(\"")?.get(1)?.replace("\")", "")?.toHTTPS()
                        val animeImage =
                            "https://animotaku.fr/wp-content/uploads/2021/08/anime-komi-cant-communicate-date-sortie.jpeg"
                        val animeGenres = Genre.getGenres(
                            document.selectXpath("//*[@id=\"section-more-details\"]/div[2]/div[2]/div[2]").text()
                                .split(",")
                        )
                        val animeDescription =
                            document.selectXpath("//*[@id=\"seasons-and-episodes-list-container\"]/div/div[1]/p").text()
                        val season =
                            document.getElementsByClass("select-label").text().split(" ")[1].toLongOrNull() ?: 1
                        val number = fet?.split(". ")?.get(0)?.toLongOrNull() ?: -1
                        val episodeType = EpisodeType.EPISODE
                        val langType = LangType.SUBTITLES
                        val title = fet?.split(". ")?.get(1)
                        val image = latestEpisode?.getElementsByClass("episode-thumbnail-image")?.attr("src")?.toHTTPS()
                            ?: return@tryCatch
                        val duration =
                            latestEpisode.getElementsByClass("episode-runtime").text().split(" min")[0].toLongOrNull()
                                ?.times(60) ?: 1440

                        this.addCheckEpisodes(id.toString())
                        list.add(
                            Episode(
                                platformImpl,
                                countryImpl,
                                releaseDate!!,
                                anime,
                                animeImage,
                                animeGenres,
                                animeDescription,
                                season,
                                number,
                                episodeType,
                                langType,
                                "$id$season$number",
                                title,
                                url,
                                image,
                                duration
                            )
                        )
                    }
                }
            }
        }


        return list.toTypedArray()
    }

    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}