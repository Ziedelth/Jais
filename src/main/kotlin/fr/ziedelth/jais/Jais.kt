/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.ScantradPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.ISO8601
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
    private var day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    @JvmStatic
    fun main(args: Array<String>) {
        JLogger.info("Init...")

        PluginManager.loadAll()

        JLogger.info("Adding countries...")
        this.addCountry(FranceCountry::class.java)

        JLogger.info("Adding platforms...")
        this.addPlatform(AnimeDigitalNetworkPlatform::class.java)
        this.addPlatform(CrunchyrollPlatform::class.java)
        this.addPlatform(ScantradPlatform::class.java)
        this.addPlatform(WakanimPlatform::class.java)

//        this.platforms.forEach { it.platform.checkEpisodes() }

        JThread.start({
            val checkDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

            if (checkDay != day) {
                JLogger.info("Resetting checked episodes...")
                day = checkDay
                this.platforms.forEach { it.platform.checkedEpisodes.clear() }
            }

            JLogger.info("Checking episodes and scans...")
            this.checkEpisodesAndScans()
            JLogger.info("All episodes and scans are checked!")
        }, delay = 2 * 60 * 1000L, priority = Thread.MAX_PRIORITY)
    }

    private fun checkEpisodesAndScans(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            val connection = Mapper.getConnection()

            val episodesList = this.platforms.flatMap {
                JLogger.info("Fetch ${it.platformHandler.name} episodes...")
                it.platform.checkEpisodes(calendar).toList()
            }

            JLogger.config("Fetched episodes length: ${episodesList.size}")

            if (episodesList.isNotEmpty()) {
                episodesList.sortedBy { it.releaseDate }.forEach { episode ->
                    val platformData = Mapper.insertPlatform(
                        connection,
                        episode.platform.platformHandler.name,
                        episode.platform.platformHandler.url,
                        episode.platform.platformHandler.image
                    )
                    val countryData = Mapper.insertCountry(
                        connection,
                        episode.country.countryHandler.name,
                        episode.country.countryHandler.flag
                    )

                    if (platformData != null && countryData != null) {
                        val animeData = Mapper.insertAnime(
                            connection,
                            countryData.id,
                            platformData.id,
                            ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
                            episode.anime,
                            episode.animeImage,
                            episode.animeDescription
                        )

                        if (animeData != null) {
                            episode.animeGenres.forEach { Mapper.insertAnimeGenre(connection, animeData.id, it.name) }

                            val exists = Mapper.getEpisode(connection, episode.episodeId) != null
                            val episodeData = Mapper.insertEpisode(
                                connection,
                                animeData.id,
                                ISO8601.toUTCDate(ISO8601.fromCalendar(episode.releaseDate)),
                                episode.season.toInt(),
                                episode.number.toInt(),
                                episode.episodeType.name,
                                episode.langType.name,
                                episode.episodeId,
                                episode.title,
                                episode.url,
                                episode.image,
                                episode.duration
                            )

                            if (!exists && episodeData != null) Impl.tryCatch {
                                PluginManager.plugins?.forEach {
                                    it.newEpisode(episode)
                                }
                            }
                        }
                    }
                }
            }

            val scansList = this.platforms.flatMap {
                JLogger.info("Fetch ${it.platformHandler.name} scans...")
                it.platform.checkScans(calendar).toList()
            }

            JLogger.config("Fetched scans length: ${scansList.size}")

            if (scansList.isNotEmpty()) {
                scansList.sortedBy { it.releaseDate }.forEach { scan ->
                    val platformData = Mapper.insertPlatform(
                        connection,
                        scan.platform.platformHandler.name,
                        scan.platform.platformHandler.url,
                        scan.platform.platformHandler.image
                    )
                    val countryData = Mapper.insertCountry(
                        connection,
                        scan.country.countryHandler.name,
                        scan.country.countryHandler.flag
                    )

                    if (platformData != null && countryData != null) {
                        val animeData = Mapper.insertAnime(
                            connection,
                            countryData.id,
                            platformData.id,
                            ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
                            scan.anime,
                            scan.animeImage,
                            scan.animeDescription
                        )

                        if (animeData != null) {
                            scan.animeGenres.forEach { Mapper.insertAnimeGenre(connection, animeData.id, it.name) }

                            val exists = Mapper.getScan(connection, animeData.id, scan.number.toInt()) != null
                            val episodeData = Mapper.insertScan(
                                connection,
                                animeData.id,
                                ISO8601.toUTCDate(ISO8601.fromCalendar(scan.releaseDate)),
                                scan.number.toInt(),
                                scan.episodeType.name,
                                scan.langType.name,
                                scan.url
                            )

                            if (!exists && episodeData != null) Impl.tryCatch {
                                PluginManager.plugins?.forEach {
                                    it.newScan(scan)
                                }
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