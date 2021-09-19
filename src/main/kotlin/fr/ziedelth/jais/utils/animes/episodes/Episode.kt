/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.Jais

data class Episode(
    val platform: String,
    val country: String,
    val releaseDate: String,
    val anime: String,
    val season: Long,
    val number: String,
    val episodeType: EpisodeType
)

enum class EpisodeType {
    SUBTITLES,
    VOICE,
    ;

    companion object {
        fun getEpisodeType(string: String?): EpisodeType? {
            if (!string.isNullOrBlank()) {
                for (information in Jais.getCountriesInformation()) {
                    if (information.countryHandler.subtitles.contains(string, true)) return SUBTITLES
                    else if (information.countryHandler.dubbed.contains(string, true)) return VOICE
                }
            }

            return null
        }
    }
}