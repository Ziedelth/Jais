/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.countries

import fr.ziedelth.jais.platforms.AnimeDigitalNetworkPlatform
import fr.ziedelth.jais.platforms.CrunchyrollPlatform
import fr.ziedelth.jais.platforms.WakanimPlatform
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.Platform

@CountryHandler(
    name = "France",
    flag = "\uD83C\uDDEB\uD83C\uDDF7",
    season = "Saison"
)
class FranceCountry : Country {
    override fun checkOnNewsURL(platform: Platform): String? {
        return when (platform::class.java) {
            AnimeDigitalNetworkPlatform::class.java -> "frFR"
            else -> null
        }
    }

    override fun checkOnEpisodesURL(platform: Platform): String? {
        return when (platform::class.java) {
            AnimeDigitalNetworkPlatform::class.java -> "fr"
            CrunchyrollPlatform::class.java -> "frFR"
            WakanimPlatform::class.java -> "fr"
            else -> null
        }
    }

    override fun restrictionEpisodes(platform: Platform): String? {
        return when (platform::class.java) {
            CrunchyrollPlatform::class.java -> "fr"
            else -> null
        }
    }

    override fun subtitlesEpisodes(platform: Platform): String? {
        return when (platform::class.java) {
            AnimeDigitalNetworkPlatform::class.java -> "vostf"
            CrunchyrollPlatform::class.java -> "fr - fr"
            else -> null
        }
    }

    override fun dubbedEpisodes(platform: Platform): String? {
        return when (platform::class.java) {
            AnimeDigitalNetworkPlatform::class.java -> "vf"
            else -> null
        }
    }
}