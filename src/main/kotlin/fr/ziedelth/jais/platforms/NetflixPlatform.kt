/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.platforms

import com.google.gson.Gson
import com.google.gson.JsonArray
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.Episode
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.sql.JMapper
import java.io.FileReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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

    @Synchronized
    override fun checkEpisodes(calendar: Calendar): Array<Episode> {
        val pairPlatformImpl = this.getPlatformImpl() ?: return emptyArray()
        val list = mutableListOf<Episode>()
        val configuration = Configuration.load() ?: return emptyArray()

        this.getAllowedCountries().forEach { pairCountryImpl ->
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
                        val client = HttpClient.newHttpClient()

                        val request = HttpRequest.newBuilder()
                            .uri(URI.create("https://unogsng.p.rapidapi.com/episodes?netflixid=$id"))
                            .header("X-RapidAPI-Host", "unogsng.p.rapidapi.com")
                            .header("X-RapidAPI-Key", configuration.key)
                            .build()

                        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                        // Convert response to json
                        val array = Gson().fromJson(response.body(), JsonArray::class.java)
                        val episodeArray = array.firstOrNull()?.asJsonObject?.get("episodes")?.asJsonArray
                        var episodeO: Episode?
                        var subtract = 4

                        JLogger.config("Init database connection")
                        val bddConnection = JMapper.getConnection()
                        JLogger.config("Database connection: ${bddConnection?.isValid(0) ?: false}")

                        do {
                            JLogger.config("Getting episode $subtract")
                            // Get the third last
                            val episode = episodeArray?.get(episodeArray.size() - subtract)?.asJsonObject

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

                            episodeO = Episode(
                                pairPlatformImpl,
                                pairCountryImpl,
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
                                "https://www.netflix.com/${pairCountryImpl.second.checkOnEpisodesURL(this)}/title/$id",
                                image,
                                1440
                            )

                            if (--subtract == 0) break
                        } while (try {
                                JLogger.config("Checking episode ${episodeO?.episodeId}")
                                val isExist = JMapper.episodeMapper.get(bddConnection, episodeO!!.episodeId) != null
                                JLogger.config("Episode ${episodeO.episodeId} is exist: $isExist")
                                isExist
                            } catch (e: Exception) {
                                JLogger.warning("Failed to check episode ${episodeO?.episodeId}: ${e.message}")
                                subtract = 4
                                false
                            }
                        )

                        bddConnection?.close()
                        this.addCheck(id.toString())
                        list.add(episodeO!!)
                    }
                }
            }
        }


        return list.toTypedArray()
    }

    private fun getISODate(calendar: Calendar): String = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
}