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
    val type: EpisodeType,

    var eId: String,
    val title: String?,
    val url: String?,
    val image: String?,
    val duration: Long
) {
    init {
        val countryHandler = Jais.getCountryInformation(this.country)!!.countryHandler
        this.eId = "${
            this.platform.uppercase().substring(0 until 4)
        }-${this.eId}-${if (this.type == EpisodeType.SUBTITLES) countryHandler.subtitles else countryHandler.dubbed}"
    }
}

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