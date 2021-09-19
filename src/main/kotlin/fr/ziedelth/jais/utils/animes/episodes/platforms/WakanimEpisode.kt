/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.platforms.Platform

data class WakanimEpisode(
    var platform: Platform? = null,
    var country: Country? = null,
    val releaseDate: String?,
    val anime: String?,
    val season: String?,
    val number: String?,
    val episodeType: EpisodeType?,

    val episodeId: Long?,
    val image: String?,
    val duration: Long?,
    val url: String?
) {
    fun isValid(): Boolean = this.platform != null &&
            this.country != null &&
            !this.releaseDate.isNullOrBlank() &&
            !this.anime.isNullOrBlank() &&
            !this.number.isNullOrBlank() &&
            this.episodeType != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = Jais.getPlatformInformation(this.platform)!!.platformHandler.name,
            country = Jais.getCountryInformation(this.country)!!.countryHandler.name,
            releaseDate = ISO8601.fromCalendar1(this.releaseDate)!!,
            anime = this.anime!!,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.number!!,
            episodeType = this.episodeType!!
        ) else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WakanimEpisode

        if (platform != other.platform) return false
        if (country != other.country) return false
        if (releaseDate != other.releaseDate) return false
        if (anime != other.anime) return false
        if (season != other.season) return false
        if (number != other.number) return false
        if (episodeType != other.episodeType) return false
        if (episodeId != other.episodeId) return false
        if (image != other.image) return false
        if (duration != other.duration) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = platform?.hashCode() ?: 0
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + (anime?.hashCode() ?: 0)
        result = 31 * result + season.hashCode()
        result = 31 * result + (number?.hashCode() ?: 0)
        result = 31 * result + (episodeType?.hashCode() ?: 0)
        result = 31 * result + (episodeId?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "WakanimEpisode(platform=$platform, country=$country, releaseDate=$releaseDate, anime=$anime, season='$season', number=$number, episodeType=$episodeType, episodeId=$episodeId, image=$image, duration=$duration, url=$url)"
    }
}