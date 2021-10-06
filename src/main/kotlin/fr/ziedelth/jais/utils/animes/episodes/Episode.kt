/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.Jais

data class Episode(
    val platform: String,
    val country: String,
    val releaseDate: String,
    var anime: String,
    val season: Long,
    var number: Long,
    val episodeType: EpisodeType,
    val langType: LangType,

    var eId: String,
    val title: String?,
    val url: String?,
    val image: String?,
    val duration: Long
) {
    init {
        this.anime = this.anime.replace("’", "'")

        val countryHandler = Jais.getCountryInformation(this.country)!!.countryHandler
        this.eId = "${
            this.platform.uppercase().substring(0 until 4)
        }-${this.eId}-${if (this.langType == LangType.SUBTITLES) countryHandler.subtitles else countryHandler.dubbed}"
    }
}

enum class EpisodeType {
    EPISODE,
    FILM,
    SPECIAL,
    ;
}

enum class LangType {
    SUBTITLES,
    VOICE,
    ;

    companion object {
        fun getLangType(string: String?): LangType? {
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