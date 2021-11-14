/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import fr.ziedelth.jais.utils.animes.sql.Mapper
import fr.ziedelth.jais.utils.debug.JLogger
import fr.ziedelth.jais.utils.debug.JThread
import fr.ziedelth.jais.utils.plugins.PluginManager
import java.util.*
import kotlin.reflect.KClass

object Jais {
    private val countries = mutableListOf<CountryImpl>()
    private val platforms = mutableListOf<PlatformImpl>()

    @JvmStatic
    fun main(args: Array<String>) {
        JLogger.info("Init...")

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)

        JThread.start({
            JLogger.info("Checking episodes...")
            this.checkEpisodes()
            JLogger.info("All episodes are checked!")
        }, delay = 300000L, priority = Thread.MAX_PRIORITY)
    }

    private fun checkEpisodes(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            val connection = Mapper.getConnection()

            val list = this.platforms.flatMap { it.platform.checkEpisodes(calendar).toList() }
            JLogger.config("Fetched episodes length: ${list.size}")

            if (list.isNotEmpty()) {
                list.forEach { episode ->
                    val platformImpl = this.getPlatformInformation(episode.platform)!!
                    val countryImpl = this.getCountryInformation(episode.country)!!
                    val platformData = Mapper.insertPlatform(
                        connection,
                        platformImpl.platformHandler.name,
                        platformImpl.platformHandler.url,
                        platformImpl.platformHandler.image
                    )
                    val countryData = Mapper.insertCountry(
                        connection,
                        countryImpl.countryHandler.name,
                        countryImpl.countryHandler.flag
                    )

                    if (platformData != null && countryData != null) {
                        val animeData = Mapper.insertAnime(
                            connection,
                            countryData.id,
                            episode.releaseDate,
                            episode.anime,
                            episode.animeImage,
                            episode.animeDescription
                        )

                        if (animeData != null) {
                            episode.animeGenres.forEach { Mapper.insertAnimeGenre(connection, animeData.id, it.name) }

                            if (Mapper.insertEpisode(
                                    connection,
                                    animeData.id,
                                    platformData.id,
                                    episode.releaseDate,
                                    episode.season.toInt(),
                                    episode.number.toInt(),
                                    episode.episodeType.name,
                                    episode.langType.name,
                                    episode.eId,
                                    episode.title,
                                    episode.url!!,
                                    episode.image,
                                    episode.duration
                                )
                            ) {
                                PluginManager.plugins.forEach { it.newEpisode(episode) }
                            }
                        }
                    }
                }
            }

            connection?.close()
        }
    }

    private fun addCountry(country: Class<out Country>) {
        if (this.countries.none { it.country::class.java == country } && country.isAnnotationPresent(CountryHandler::class.java)) {
            this.countries.add(
                CountryImpl(
                    countryHandler = country.getAnnotation(CountryHandler::class.java),
                    country = country.getConstructor().newInstance()
                )
            )
        } else JLogger.warning("Failed to add ${country.simpleName}")
    }

    private fun addPlatform(platform: Class<out Platform>) {
        if (this.platforms.none { it.platform::class.java == platform } && platform.isAnnotationPresent(PlatformHandler::class.java)) {
            this.platforms.add(
                PlatformImpl(
                    platformHandler = platform.getAnnotation(PlatformHandler::class.java),
                    platform = platform.getConstructor().newInstance()
                )
            )
        } else JLogger.warning("Failed to add ${platform.simpleName}")
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

    fun getPlatformInformation(platform: Platform?): PlatformImpl? {
        return if (platform != null) this.platforms.firstOrNull { it.platform::class.java == platform::class.java } else null
    }

    fun getPlatformInformation(platform: String?): PlatformImpl? {
        return if (!platform.isNullOrBlank()) this.platforms.firstOrNull {
            it.platformHandler.name.equals(
                platform,
                true
            )
        } else null
    }

    fun getAllowedCountries(platform: Platform?): Array<Country> {
        return this.getPlatformInformation(platform)?.platformHandler?.countries?.mapNotNull { platformCountry ->
            this.getCountryInformation(
                platformCountry
            )
        }?.map { it.country }?.toTypedArray() ?: arrayOf()
    }
}