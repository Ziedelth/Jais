/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

@PlatformHandler(
    name = "Netflix",
    url = "https://netflix.com/",
    image = "images/platforms/netflix.png",
    color = 0xE50914,
    countries = [FranceCountry::class]
)
class NetflixPlatform(jais: Jais) : Platform(jais) {
    data class Configuration(val key: String) {
        companion object {
            fun load(): Configuration? {
                val file = FileImpl.getFile("netflix.json")
                return if (!file.exists()) null else Gson().fromJson(FileReader(file), Configuration::class.java)
            }
        }
    }


    /* It's a `@Synchronized` annotation. It's a way to prevent multiple threads to access the same method at the same
    time. */
    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val platformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val configuration = Configuration.load() ?: return emptyArray()

        this.getAllowedCountries().forEach { country ->
            val countryImpl = this.jais.getCountryInformation(country) ?: return@forEach

            Impl.tryCatch("Failed to get ${this.javaClass.simpleName} episode(s):") {
                if (calendar.get(Calendar.DAY_OF_WEEK) == 4) {
                    // Komi can't communicate
                    val id = 81228573

                    val releaseDate = ISO8601.fromUTCDate("${getISODate(calendar)}T07:02:00Z")
                    if (!this.checkedEpisodes.contains(id.toString()) && ISO8601.isSameDayUsingInstant(
                            calendar,
                            releaseDate
                        ) && calendar.after(releaseDate)
                    ) {
                        val client = OkHttpClient()

                        val request = Request.Builder()
                            .url("https://unogsng.p.rapidapi.com/episodes?netflixid=$id")
                            .get()
                            .addHeader("X-RapidAPI-Host", "unogsng.p.rapidapi.com")
                            .addHeader("X-RapidAPI-Key", configuration.key)
                            .build()

                        val response = client.newCall(request).execute()
                        // Convert response to json
                        val array = Gson().fromJson(response.body()?.string(), JsonArray::class.java)
                        val episodeArray = array.firstOrNull()?.asJsonObject?.get("episodes")?.asJsonArray
                        // Get the third last
                        val episode = episodeArray?.get(episodeArray.size() - 4)?.asJsonObject

                        val anime = "Komi cherche ses mots"
                        val animeImage =
                            "https://animotaku.fr/wp-content/uploads/2021/08/anime-komi-cant-communicate-date-sortie.jpeg"
                        val animeGenres =
                            arrayOf(Genre.COMEDY, Genre.DRAMA, Genre.ROMANCE, Genre.SCHOOL, Genre.SLICE_OF_LIFE)
                        val animeDescription =
                            "Dans un lycée regorgeant de personnalités pour le moins originales, Tadano aide sa camarade Komi, timide et peu sociable, à atteindre son objectif : se faire 100 amis."
                        val season = episode?.get("seasnum")?.asLong ?: return@tryCatch
                        val number = episode.get("epnum")?.asLong ?: return@tryCatch
                        val episodeType = EpisodeType.EPISODE
                        val langType = LangType.SUBTITLES
                        val image = episode.get("img")?.asString ?: return@tryCatch

                        this.addCheck(id.toString())
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
                                null,
                                "https://www.netflix.com/${country.checkOnEpisodesURL(this)}/title/$id",
                                image,
                                1440
                            )
                        )
                    }
                }
            }
        }


        return list.toTypedArray()
    }

    /**
     * Get the date in ISO format
     *
     * @param calendar The calendar object that you want to convert to a string.
     */
    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}