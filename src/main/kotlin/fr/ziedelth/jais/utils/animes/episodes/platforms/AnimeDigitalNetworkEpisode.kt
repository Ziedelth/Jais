/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.episodes.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler

data class AnimeDigitalNetworkEpisode(
    val releaseDate: String?,
    val show: Show?,
    val season: String?,
    val shortNumber: String?,
    val languages: Array<String>?,

    val id: Long?,
    val name: String?,
    val image: String?,
    val duration: Long?,
    val url: String?,
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
            this.show != null && (!this.show.shortTitle.isNullOrBlank() || !this.show.title.isNullOrBlank()) &&
            !this.shortNumber.isNullOrBlank() && this.shortNumber.toLongOrNull() != null &&
            this.languages != null && LangType.getLangType(this.languages.lastOrNull()) != LangType.UNKNOWN &&
            this.id != null &&
            this.duration != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformHandler!!.name,
            country = this.countryHandler!!.name,
            releaseDate = ISO8601.fromCalendar1(this.releaseDate)!!,
            anime = this.show!!.shortTitle ?: this.show.title!!,
            animeImage = this.show.image2x,
            animeGenres = AnimeGenre.getGenres(
                this.show.genres?.flatMap { it.split(" / ") }?.toTypedArray() ?: emptyArray()
            ),
            season = this.season?.toLongOrNull() ?: 1,
            number = this.shortNumber?.toLongOrNull() ?: 1,
            episodeType = EpisodeType.EPISODE,
            langType = LangType.getLangType(this.languages!!.lastOrNull()),

            eId = this.id!!.toString(),
            title = this.name,
            url = this.url,
            image = this.image,
            duration = this.duration!!,
        ) else null
    }
}

data class Show(
    val title: String?,
    val image2x: String?,
    val shortTitle: String?,
    val genres: Array<String>?,
)
