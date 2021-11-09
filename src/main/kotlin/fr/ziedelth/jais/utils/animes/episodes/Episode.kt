/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601

data class Episode(
    val platform: String,
    val country: String,
    var releaseDate: String,
    var anime: String,
    var animeImage: String?,
    val animeGenres: Array<AnimeGenre>,
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
        this.releaseDate = ISO8601.toUTCDate(this.releaseDate)
        this.anime = this.anime.replace("’", "'")

        val countryInformation = Jais.getCountryInformation(this.country)
        this.eId = "${
            this.platform.uppercase().substring(0 until 4)
        }-${this.eId}-${this.langType.getData(countryInformation?.country?.javaClass)?.data}"
    }
}