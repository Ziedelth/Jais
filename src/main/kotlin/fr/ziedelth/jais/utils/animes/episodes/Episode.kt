/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.platforms.Platform
import java.util.*

data class Episode(
    val platform: Platform,
    val country: Country,
    val releaseDate: Calendar,
    val anime: String,
    val season: Long,
    val number: String,
    val episodeType: EpisodeType
)

enum class EpisodeType {
    SUBTITLES,
    VOICE,
    ;
}