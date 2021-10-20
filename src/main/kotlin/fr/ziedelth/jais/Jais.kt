/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.ziedelth.jais.countries.FranceCountry
import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.*
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.util.*
import kotlin.reflect.KClass

object Jais {
    private val countries = mutableListOf<CountryImpl>()
    private val platforms = mutableListOf<PlatformImpl>()
    private val comparator = Comparator.comparing { episode: Episode -> ISO8601.toCalendar1(episode.releaseDate) }
        .thenComparing(Episode::anime).thenComparing(Episode::season).thenComparing(Episode::number)

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
        }, delay = 120000L, priority = Thread.MAX_PRIORITY)
    }

    private fun checkEpisodes(calendar: Calendar = Calendar.getInstance()) {
        Impl.tryCatch("Failed to fetch episodes") {
            val list = this.platforms.flatMap { it.platform.checkEpisodes(calendar).toList() }
            JLogger.config("Fetched episodes length: ${list.size}")

            if (list.isNotEmpty()) {
                val gson = GsonBuilder().setPrettyPrinting().create()

                val fileMinimize = File("episodes.minimize.json")
                val file = File("episodes.json")

                val episodeImpl = if (!file.exists()) {
                    file.createNewFile()
                    Files.writeString(file.toPath(), gson.toJson(JsonObject()))
                    EpisodeImpl()
                } else {
                    gson.fromJson(FileReader(file), EpisodeImpl::class.java)
                }

                if (episodeImpl != null) {
                    list.sortedWith(this.comparator).forEach { episode ->
                        val platformImpl = this.getPlatformInformation(episode.platform)!!
                        val countryImpl = this.getCountryInformation(episode.country)!!
                        val pImpl = episodeImpl.insertOrUpdatePlatform(platformImpl.platformHandler)
                        val cImpl = episodeImpl.insertOrUpdateCountry(countryImpl.countryHandler)

                        episodeImpl.insertOrUpdateEpisode(
                            pImpl.uuid,
                            cImpl.uuid,
                            episode
                        )
                    }

                    episodeImpl.update()
                }


                Files.writeString(fileMinimize.toPath(), Gson().toJson(episodeImpl))
                Files.writeString(file.toPath(), gson.toJson(episodeImpl))

                val add = file.length() - fileMinimize.length()
                JLogger.config("Minimize episodes file weight: ${FileImpl.toFormat(fileMinimize.length())}")
                JLogger.config(
                    "Episodes file weight: ${FileImpl.toFormat(file.length())} (${this.addOrMinus(add)}${
                        FileImpl.toFormat(
                            add
                        )
                    })"
                )
            }
        }
    }

    private fun addOrMinus(number: Long): String = if (number > 0) "+" else "-"

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