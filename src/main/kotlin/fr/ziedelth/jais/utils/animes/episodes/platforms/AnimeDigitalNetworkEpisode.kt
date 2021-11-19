/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl

data class AnimeDigitalNetworkEpisode(
    val releaseDate: String?,
    val show: Show?,
    val season: String?,
    val shortNumber: String?,
    val languages: Array<String>?,
    val id: Long?,
    val name: String?,
    val image2x: String?,
    val duration: Long?,
    val url: String?,
) {
    data class Show(
        val title: String?,
        val image2x: String?,
        val shortTitle: String?,
        val summary: String?,
        val genres: Array<String>?,
    )

    var platformImpl: PlatformImpl? = null
    var countryImpl: CountryImpl? = null

    fun isValid(): Boolean = this.platformImpl != null &&
            this.countryImpl != null &&
            ISO8601.fromUTCDate(ISO8601.toUTCDate(this.releaseDate)) != null &&
            this.show != null &&
            (!this.show.shortTitle.isNullOrBlank() || !this.show.title.isNullOrBlank()) &&
            !this.show.image2x.isNullOrBlank() &&
            this.languages != null && LangType.getLangType(this.languages.lastOrNull()) != LangType.UNKNOWN &&
            this.id != null &&
            !this.url.isNullOrBlank() &&
            !this.image2x.isNullOrBlank()

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformImpl!!,
            country = this.countryImpl!!,
            releaseDate = ISO8601.fromUTCDate(this.releaseDate)!!,
            anime = this.show!!.shortTitle ?: this.show.title!!,
            animeImage = this.show.image2x!!.replace("http://", "https://"),
            animeGenres = AnimeGenre.getGenres(
                this.show.genres?.flatMap { it.split(" / ") }?.toTypedArray() ?: emptyArray()
            ),
            animeDescription = this.show.summary,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.shortNumber?.toLongOrNull() ?: -1,
            episodeType = EpisodeType.EPISODE,
            langType = LangType.getLangType(this.languages!!.lastOrNull()),
            episodeId = this.id!!.toString(),
            title = this.name,
            url = this.url!!.replace("http://", "https://"),
            image = this.image2x!!.replace("http://", "https://"),
            duration = this.duration ?: -1,
        ) else null
    }
}
