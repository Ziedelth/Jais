/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.platforms.Platform

data class AnimeDigitalNetworkEpisode(
    val id: Long?,
    val name: String?,
    val shortNumber: String?,
    val season: String?,
    val image: String?,
    val releaseDate: String?,
    val duration: Long?,
    val url: String?,
    val languages: Array<String>?,
    val show: Show?,
) {
    var platform: Platform? = null
    var country: Country? = null

    fun isValid(): Boolean = this.platform != null &&
            this.country != null &&
            !this.releaseDate.isNullOrBlank() &&
            this.show != null && (!this.show.title.isNullOrBlank() || !this.show.originalTitle.isNullOrBlank()) &&
            this.season != null &&
            this.shortNumber != null &&
            this.languages != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platform!!,
            country = this.country!!,
            releaseDate = ISO8601.toCalendar1(this.releaseDate!!)!!,
            anime = if (!this.show!!.originalTitle.isNullOrBlank()) this.show.originalTitle!! else this.show.title!!,
            season = this.season!!.toLongOrNull() ?: 1,
            number = this.shortNumber!!,
            episodeType = if (this.languages!!.contains(this.country!!.dubbedEpisodes(this.platform!!))) EpisodeType.VOICE else EpisodeType.SUBTITLES
        ) else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimeDigitalNetworkEpisode

        if (id != other.id) return false
        if (name != other.name) return false
        if (shortNumber != other.shortNumber) return false
        if (season != other.season) return false
        if (image != other.image) return false
        if (releaseDate != other.releaseDate) return false
        if (duration != other.duration) return false
        if (url != other.url) return false
        if (languages != null) {
            if (other.languages == null) return false
            if (!languages.contentEquals(other.languages)) return false
        } else if (other.languages != null) return false
        if (show != other.show) return false
        if (platform != other.platform) return false
        if (country != other.country) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (shortNumber?.hashCode() ?: 0)
        result = 31 * result + (season?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (languages?.contentHashCode() ?: 0)
        result = 31 * result + (show?.hashCode() ?: 0)
        result = 31 * result + (platform?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AnimeDigitalNetworkEpisode(id=$id, name=$name, shortNumber=$shortNumber, season=$season, image=$image, releaseDate=$releaseDate, duration=$duration, url=$url, languages=${languages?.contentToString()}, show=$show, platform=$platform, country=$country)"
    }
}

data class Show(
    val title: String?,
    val originalTitle: String?
)
