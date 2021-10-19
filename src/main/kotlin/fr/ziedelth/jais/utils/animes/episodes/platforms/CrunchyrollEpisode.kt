/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import com.google.gson.JsonObject
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler

data class CrunchyrollEpisode(
    val title: String?,
    val pubDate: String?,
    val seriesTitle: String?,
    var seriesImage: String? = null,
    val season: String?,
    val episodeNumber: String?,
    val restriction: JsonObject?,
    val subtitleLanguages: String?,

    val mediaId: String?,
    val episodeTitle: String?,
    val thumbnail: Array<Thumbnail>?,
    val duration: Long? = null,
    val link: String?,
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
            !this.pubDate.isNullOrBlank() &&
            !this.seriesTitle.isNullOrBlank() &&
            !this.episodeNumber.isNullOrBlank() &&
            this.restriction != null && this.restriction.get("").asString.split(" ")
        .contains(this.country!!.restrictionEpisodes(this.platform!!)) &&
            (this.title?.contains(
                "(${this.countryHandler?.dubbed})",
                true
            ) == true || (!this.subtitleLanguages.isNullOrBlank() && this.subtitleLanguages.split(",")
                .contains(this.country!!.subtitlesEpisodes(this.platform!!)))) &&
            !this.mediaId.isNullOrBlank() &&
            this.duration != null

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformHandler!!.name,
            country = this.countryHandler!!.name,
            releaseDate = ISO8601.fromCalendar2(this.pubDate)!!,
            anime = this.seriesTitle!!,
            animeImage = this.seriesImage,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.episodeNumber?.toLongOrNull() ?: -1,
            episodeType = if ((this.episodeNumber?.toLongOrNull()
                    ?: -1L) != -1L
            ) EpisodeType.EPISODE else EpisodeType.SPECIAL,
            langType = if (this.country?.subtitlesEpisodes(this.platform!!) == this.subtitleLanguages || this.title?.contains(
                    "(${this.countryHandler?.dubbed})",
                    true
                ) == true
            ) LangType.VOICE else LangType.SUBTITLES,

            eId = this.mediaId!!,
            title = this.episodeTitle,
            url = this.link,
            image = this.thumbnail?.maxByOrNull {
                it.width?.toLongOrNull()?.times(it.height?.toLongOrNull() ?: 0) ?: 0
            }?.url,
            duration = this.duration!!,
        ) else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CrunchyrollEpisode

        if (title != other.title) return false
        if (pubDate != other.pubDate) return false
        if (seriesTitle != other.seriesTitle) return false
        if (season != other.season) return false
        if (episodeNumber != other.episodeNumber) return false
        if (restriction != other.restriction) return false
        if (subtitleLanguages != other.subtitleLanguages) return false
        if (mediaId != other.mediaId) return false
        if (episodeTitle != other.episodeTitle) return false
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false
        if (duration != other.duration) return false
        if (link != other.link) return false
        if (platformHandler != other.platformHandler) return false
        if (platform != other.platform) return false
        if (countryHandler != other.countryHandler) return false
        if (country != other.country) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (pubDate?.hashCode() ?: 0)
        result = 31 * result + (seriesTitle?.hashCode() ?: 0)
        result = 31 * result + (season?.hashCode() ?: 0)
        result = 31 * result + (episodeNumber?.hashCode() ?: 0)
        result = 31 * result + (restriction?.hashCode() ?: 0)
        result = 31 * result + (subtitleLanguages?.hashCode() ?: 0)
        result = 31 * result + (mediaId?.hashCode() ?: 0)
        result = 31 * result + (episodeTitle?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (platformHandler?.hashCode() ?: 0)
        result = 31 * result + (platform?.hashCode() ?: 0)
        result = 31 * result + (countryHandler?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CrunchyrollEpisode(title=$title, pubDate=$pubDate, seriesTitle=$seriesTitle, season=$season, episodeNumber=$episodeNumber, restriction=$restriction, subtitleLanguages=$subtitleLanguages, mediaId=$mediaId, episodeTitle=$episodeTitle, thumbnail=${thumbnail?.contentToString()}, duration=$duration, link=$link, platformHandler=$platformHandler, platform=$platform, countryHandler=$countryHandler, country=$country)"
    }
}

data class Thumbnail(
    val url: String?,
    val width: String?,
    val height: String?
)

