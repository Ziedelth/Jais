/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.countries

import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.NetflixPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.Platform

@CountryHandler(
    tag = "fr",
    name = "France",
    flag = "\uD83C\uDDEB\uD83C\uDDF7",
    season = "Saison"
)
class FranceCountry : Country {
    /**
     * When the platform is AnimeDigitalNetworkPlatform, return "frFR". Otherwise, return null
     *
     * @param platform Platform? -> The platform that the URL is being checked on.
     * @return The language code for the platform.
     */
    override fun checkOnNewsURL(platform: Platform?): String? {
        return when (platform?.javaClass) {
            AnimeDigitalNetworkPlatform::class.java -> "frFR"
            else -> null
        }
    }

    /**
     * When the platform is either Anime Digital Network, Netflix, or Wakanim, return "fr"
     *
     * @param platform Platform? -> The platform that the URL is being checked for.
     * @return The language code for the platform.
     */
    override fun checkOnEpisodesURL(platform: Platform?): String? {
        return when (platform?.javaClass) {
            AnimeDigitalNetworkPlatform::class.java, NetflixPlatform::class.java, WakanimPlatform::class.java -> "fr"
            CrunchyrollPlatform::class.java -> "frFR"
            else -> null
        }
    }

    /**
     * "When the platform is Crunchyroll, return the French language."
     *
     * The `when` statement is a replacement for the `if` statement. It's a little more readable than the `if` statement,
     * and it's also more powerful
     *
     * @param platform The platform to check.
     * @return The string "fr"
     */
    override fun restrictionEpisodes(platform: Platform?): String? {
        return when (platform?.javaClass) {
            CrunchyrollPlatform::class.java -> "fr"
            else -> null
        }
    }

    /**
     * "When the platform is AnimeDigitalNetworkPlatform, return vostf. When the platform is CrunchyrollPlatform, return fr
     * - fr."
     *
     * The function above is a good example of a function that returns a value based on the value of a variable
     *
     * @param platform Platform?
     * @return The value of the `subtitlesEpisodes` method is being returned.
     */
    override fun subtitlesEpisodes(platform: Platform?): String? {
        return when (platform?.javaClass) {
            AnimeDigitalNetworkPlatform::class.java -> "vostf"
            CrunchyrollPlatform::class.java -> "fr - fr"
            else -> null
        }
    }

    /**
     * When the platform is AnimeDigitalNetworkPlatform, return "vf". Otherwise, return null
     *
     * @param platform Platform?
     * @return The dubbed episodes for the given platform.
     */
    override fun dubbedEpisodes(platform: Platform?): String? {
        return when (platform?.javaClass) {
            AnimeDigitalNetworkPlatform::class.java -> "vf"
            else -> null
        }
    }
}