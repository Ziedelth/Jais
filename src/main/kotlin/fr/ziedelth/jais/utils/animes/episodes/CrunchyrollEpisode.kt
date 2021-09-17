/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.platforms.Platform

data class CrunchyrollEpisode(
    val mediaId: String?,
    val seriesTitle: String?,
    val episodeNumber: String?,
    val season: String?,
    val episodeTitle: String?,
    val thumbnail: Array<Thumbnail>?,
    val pubDate: String?,
    val duration: String?,
    val link: String?,
    val restriction: JsonObject?,
    val subtitleLanguages: String?
) {
    var platform: Platform? = null
    var country: Country? = null

    fun isValid(): Boolean = this.platform != null &&
            this.country != null &&
            !this.pubDate.isNullOrBlank() &&
            !this.seriesTitle.isNullOrBlank() &&
            !this.episodeNumber.isNullOrBlank() &&
            this.restriction != null && this.restriction.get("").asString.split(" ")
        .contains(this.country!!.restrictionEpisodes(this.platform!!)) &&
            !this.subtitleLanguages.isNullOrBlank() && this.subtitleLanguages.split(",")
        .contains(this.country!!.subtitlesEpisodes(this.platform!!))

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platform!!,
            country = this.country!!,
            releaseDate = ISO8601.toCalendar2(this.pubDate!!)!!,
            anime = this.seriesTitle!!,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.episodeNumber!!,
            episodeType = if (this.subtitleLanguages!! == this.country!!.subtitlesEpisodes(this.platform!!)) EpisodeType.VOICE else EpisodeType.SUBTITLES
        ) else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CrunchyrollEpisode

        if (mediaId != other.mediaId) return false
        if (seriesTitle != other.seriesTitle) return false
        if (episodeNumber != other.episodeNumber) return false
        if (season != other.season) return false
        if (episodeTitle != other.episodeTitle) return false
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false
        if (pubDate != other.pubDate) return false
        if (duration != other.duration) return false
        if (link != other.link) return false
        if (restriction != other.restriction) return false
        if (subtitleLanguages != other.subtitleLanguages) return false
        if (platform != other.platform) return false
        if (country != other.country) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mediaId?.hashCode() ?: 0
        result = 31 * result + (seriesTitle?.hashCode() ?: 0)
        result = 31 * result + (episodeNumber?.hashCode() ?: 0)
        result = 31 * result + (season?.hashCode() ?: 0)
        result = 31 * result + (episodeTitle?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (pubDate?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (restriction?.hashCode() ?: 0)
        result = 31 * result + (subtitleLanguages?.hashCode() ?: 0)
        result = 31 * result + (platform?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CrunchyrollEpisode(mediaId=$mediaId, seriesTitle=$seriesTitle, episodeNumber=$episodeNumber, season=$season, episodeTitle=$episodeTitle, thumbnail=${thumbnail?.contentToString()}, pubDate=$pubDate, duration=$duration, link=$link, restriction=$restriction, subtitleLanguages=$subtitleLanguages, platform=$platform, country=$country)"
    }
}

data class Thumbnail(
    val url: String?,
    val width: String?,
    val height: String?
)

