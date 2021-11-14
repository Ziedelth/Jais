/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler

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
    var platformHandler: PlatformHandler? = null
    var platform: Platform? = null
        set(value) {
            field = value
            this.platformHandler = Jais.getPlatformInformation(value)?.platformHandler
        }
    var countryHandler: CountryHandler? = null
    var country: Country? = null
        set(value) {
            field = value
            this.countryHandler = Jais.getCountryInformation(value)?.countryHandler
        }

    fun isValid(): Boolean = this.platform != null &&
            this.country != null &&
            !this.releaseDate.isNullOrBlank() &&
            !this.anime.isNullOrBlank() &&
            this.number != null &&
            this.episodeType != null &&
            this.langType != null &&
            this.episodeId != null &&
            this.duration != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformHandler!!.name,
            country = this.countryHandler!!.name,
            releaseDate = ISO8601.fromCalendar1(this.releaseDate)!!,
            anime = this.anime!!,
            animeImage = this.animeImage?.replace("http://", "https://"),
            animeGenres = this.animeGenres,
            animeDescription = this.animeDescription,
            season = this.season ?: 1,
            number = this.number ?: 1,
            episodeType = this.episodeType!!,
            langType = this.langType!!,

            eId = this.episodeId!!.toString(),
            title = null,
            url = this.url?.replace("http://", "https://"),
            image = this.image?.replace("http://", "https://"),
            duration = this.duration!!,
        ) else null
    }
}