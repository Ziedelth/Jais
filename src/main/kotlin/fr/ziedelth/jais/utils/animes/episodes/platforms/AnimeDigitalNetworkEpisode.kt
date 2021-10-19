/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
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
            this.show != null && !this.show.title.isNullOrBlank() &&
            !this.shortNumber.isNullOrBlank() && this.shortNumber.toLongOrNull() != null &&
            this.languages != null && LangType.getLangType(this.languages.lastOrNull()) != null &&
            this.id != null &&
            this.duration != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformHandler!!.name,
            country = this.countryHandler!!.name,
            releaseDate = ISO8601.fromCalendar1(this.releaseDate)!!,
            anime = this.show!!.title!!,
            animeImage = this.show.image2x,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.shortNumber?.toLongOrNull() ?: 1,
            episodeType = EpisodeType.EPISODE,
            langType = LangType.getLangType(this.languages!!.lastOrNull())!!,

            eId = this.id!!.toString(),
            title = this.name,
            url = this.url,
            image = this.image,
            duration = this.duration!!,
        ) else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimeDigitalNetworkEpisode

        if (releaseDate != other.releaseDate) return false
        if (show != other.show) return false
        if (season != other.season) return false
        if (shortNumber != other.shortNumber) return false
        if (languages != null) {
            if (other.languages == null) return false
            if (!languages.contentEquals(other.languages)) return false
        } else if (other.languages != null) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (image != other.image) return false
        if (duration != other.duration) return false
        if (url != other.url) return false
        if (platformHandler != other.platformHandler) return false
        if (platform != other.platform) return false
        if (countryHandler != other.countryHandler) return false
        if (country != other.country) return false

        return true
    }

    override fun hashCode(): Int {
        var result = releaseDate?.hashCode() ?: 0
        result = 31 * result + (show?.hashCode() ?: 0)
        result = 31 * result + (season?.hashCode() ?: 0)
        result = 31 * result + (shortNumber?.hashCode() ?: 0)
        result = 31 * result + (languages?.contentHashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (platformHandler?.hashCode() ?: 0)
        result = 31 * result + (platform?.hashCode() ?: 0)
        result = 31 * result + (countryHandler?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AnimeDigitalNetworkEpisode(releaseDate=$releaseDate, show=$show, season=$season, shortNumber=$shortNumber, languages=${languages?.contentToString()}, id=$id, name=$name, image=$image, duration=$duration, url=$url, platformHandler=$platformHandler, platform=$platform, countryHandler=$countryHandler, country=$country)"
    }
}

data class Show(
    val title: String?,
    val image2x: String?,
)
