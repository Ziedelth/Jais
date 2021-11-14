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

data class WakanimEpisode(
    val releaseDate: String?,
    val anime: String?,
    val animeImage: String?,
    val animeGenres: Array<AnimeGenre> = emptyArray(),
    val animeDescription: String?,
    val season: Long?,
    val number: Long?,
    val episodeType: EpisodeType?,
    val langType: LangType?,
    val episodeId: Long?,
    val image: String?,
    val duration: Long?,
    var url: String?
) {
    var platformImpl: PlatformImpl? = null
    var countryImpl: CountryImpl? = null

    fun isValid(): Boolean = this.platformImpl != null &&
            this.countryImpl != null &&
            ISO8601.fromUTCDate(ISO8601.toUTCDate(this.releaseDate)) != null &&
            !this.anime.isNullOrBlank() &&
            !this.animeImage.isNullOrBlank() &&
            this.episodeType != null && this.episodeType != EpisodeType.UNKNOWN &&
            this.langType != null && this.langType != LangType.UNKNOWN &&
            this.episodeId != null &&
            !this.url.isNullOrBlank() &&
            !this.image.isNullOrBlank() &&
            this.duration != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformImpl!!,
            country = this.countryImpl!!,
            releaseDate = ISO8601.fromUTCDate(ISO8601.toUTCDate(this.releaseDate))!!,
            anime = this.anime!!,
            animeImage = this.animeImage!!.replace("http://", "https://"),
            animeGenres = this.animeGenres,
            animeDescription = this.animeDescription,
            season = this.season ?: 1,
            number = this.number ?: -1,
            episodeType = this.episodeType!!,
            langType = this.langType!!,

            episodeId = this.episodeId!!.toString(),
            title = null,
            url = this.url!!.replace("http://", "https://"),
            image = this.image!!.replace("http://", "https://"),
            duration = this.duration!!,
        ) else null
    }
}