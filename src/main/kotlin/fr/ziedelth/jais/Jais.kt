/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.JLogger
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.sql.SQL
import java.util.*
import kotlin.reflect.KClass

object Jais {
    private var isRunning = true
    private val countries = mutableListOf<CountryImpl>()
    private val platforms = mutableListOf<PlatformImpl>()

    @JvmStatic
    fun main(args: Array<String>) {
        JLogger.info("Init...")

        JLogger.info("Adding countries...")
        if (!this.addCountry(FranceCountry::class.java))
            JLogger.warning("Failed to add France country")

        JLogger.info("Adding platforms...")
        if (!this.addPlatform(AnimeDigitalNetworkPlatform::class.java))
            JLogger.warning("Failed to add AnimeDigitalNetwork platform")
        if (!this.addPlatform(CrunchyrollPlatform::class.java))
            JLogger.warning("Failed to add Crunchyroll platform")
        if (!this.addPlatform(WakanimPlatform::class.java))
            JLogger.warning("Failed to add Wakanim platform")

        // this.platforms.forEach { it.platform.checkLastNews() }
        // this.platforms.forEach { it.platform.checkEpisodes() }
        this.checkEpisodes()

        JLogger.info("Running...")
        while (this.isRunning)
            Thread.sleep(25)
    }

    private fun checkEpisodes(calendar: Calendar = Calendar.getInstance()) {
        val connection = SQL.getConnection()

        this.platforms.forEach { platformImpl ->
            val platformSQL = SQL.psui(connection, platformImpl.platformHandler)

            platformImpl.platform.checkEpisodes(calendar).forEach { episode ->
                val countryImpl = this.getCountryInformation(episode.country)!!
                val countrySQL = SQL.csui(connection, countryImpl.countryHandler)
                val animeSQL = SQL.asi(connection, episode.anime, episode.releaseDate)

                if (platformSQL != null && countrySQL != null && animeSQL != null) {
                    var episodeSQL = SQL.getEpisodeIsInDatabase(connection, episode)

                    if (episodeSQL == null) {
                        if (!SQL.insertEpisodeInDatabase(connection, platformSQL, countrySQL, animeSQL, episode))
                            JLogger.warning("Failed to insert episode in database")
                        else {
                            episodeSQL = SQL.getEpisodeIsInDatabase(connection, episode)

                            JLogger.info("New episode in database")
                            JLogger.config("ID: ${episodeSQL?.id} | EID: ${episodeSQL?.eId} | Anime: ${episode.anime} | Title: ${episodeSQL?.title} | Episode type: ${episodeSQL?.episodeType} | Lang type: ${episodeSQL?.langType}")
                        }
                    } else {
                        if (episode.title != episodeSQL.title || episode.url != episodeSQL.url || episode.image != episodeSQL.image || episode.duration != episodeSQL.duration) {
                            if (!SQL.updateEpisodeInDatabase(
                                    connection,
                                    episode
                                )
                            ) JLogger.warning("Failed to update episode in database")
                            else {
                                episodeSQL = SQL.getEpisodeIsInDatabase(connection, episode)

                                JLogger.info("Update in database")
                                JLogger.config("ID: ${episodeSQL?.id} | EID: ${episodeSQL?.eId} | Anime: ${episode.anime} | Title: ${episodeSQL?.title} | Episode type: ${episodeSQL?.episodeType} | Lang type: ${episodeSQL?.langType}")
                            }
                        }
                    }
                }
            }
        }

        connection?.close()
    }

    private fun addCountry(country: Class<out Country>): Boolean {
        if (this.countries.none { it.country::class.java == country } && country.isAnnotationPresent(CountryHandler::class.java)) {
            return this.countries.add(
                CountryImpl(
                    countryHandler = country.getAnnotation(CountryHandler::class.java),
                    country = country.getConstructor().newInstance()
                )
            )
        }

        return false
    }

    private fun addPlatform(platform: Class<out Platform>): Boolean {
        if (this.platforms.none { it.platform::class.java == platform } && platform.isAnnotationPresent(PlatformHandler::class.java)) {
            return this.platforms.add(
                PlatformImpl(
                    platformHandler = platform.getAnnotation(PlatformHandler::class.java),
                    platform = platform.getConstructor().newInstance()
                )
            )
        }

        return false
    }

    fun getCountriesInformation(): Array<CountryImpl> {
        return this.countries.toTypedArray()
    }

    fun getCountryInformation(country: String?): CountryImpl? {
        return if (!country.isNullOrBlank()) this.countries.firstOrNull {
            it.countryHandler.name.equals(
                country,
                true
            )
        } else null
    }

    fun getCountryInformation(country: Country?): CountryImpl? {
        return if (country != null) this.countries.firstOrNull { it.country::class.java == country::class.java } else null
    }

    private fun getCountryInformation(country: KClass<out Country>?): CountryImpl? {
        return this.countries.firstOrNull { it.country::class.java == country?.java }
    }

    fun getPlatformInformation(platform: String?): PlatformImpl? {
        return if (!platform.isNullOrBlank()) this.platforms.firstOrNull {
            it.platformHandler.name.equals(
                platform,
                true
            )
        } else null
    }

    fun getPlatformInformation(platform: Platform?): PlatformImpl? {
        return if (platform != null) this.platforms.firstOrNull { it.platform::class.java == platform::class.java } else null
    }

    fun getAllowedCountries(platform: Platform?): Array<Country> {
        return this.getPlatformInformation(platform)?.platformHandler?.countries?.mapNotNull { platformCountry ->
            this.getCountryInformation(
                platformCountry
            )
        }?.map { it.country }?.toTypedArray() ?: arrayOf()
    }
}