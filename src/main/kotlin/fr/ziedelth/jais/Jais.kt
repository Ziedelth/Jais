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
import kotlin.reflect.KClass

object Jais {
    private var isRunning = true
    private val countries: MutableList<CountryImpl> = mutableListOf()
    private val platforms: MutableList<PlatformImpl> = mutableListOf()

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

        this.platforms.forEach { JLogger.config(it.platform.checkLastEpisodes().contentToString()) }

        JLogger.info("Running...")
        while (this.isRunning)
            Thread.sleep(25)
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

    fun getCountryInformation(country: Country?): CountryImpl? {
        return if (country != null) this.countries.firstOrNull { it.country::class.java == country::class.java } else null
    }

    fun getCountryInformation(country: KClass<out Country>?): CountryImpl? {
        return this.countries.firstOrNull { it.country::class.java == country?.java }
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