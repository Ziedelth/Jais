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

data class WakanimEpisode(
    val releaseDate: String?,
    val anime: String?,
    val animeImage: String?,
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
            animeImage = this.animeImage,
            season = this.season ?: 1,
            number = this.number ?: 1,
            episodeType = this.episodeType!!,
            langType = this.langType!!,

            eId = this.episodeId!!.toString(),
            title = null,
            url = this.url,
            image = this.image,
            duration = this.duration!!,
        ) else null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WakanimEpisode

        if (releaseDate != other.releaseDate) return false
        if (anime != other.anime) return false
        if (season != other.season) return false
        if (number != other.number) return false
        if (episodeType != other.episodeType) return false
        if (langType != other.langType) return false
        if (episodeId != other.episodeId) return false
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
        result = 31 * result + (anime?.hashCode() ?: 0)
        result = 31 * result + (season?.hashCode() ?: 0)
        result = 31 * result + (number?.hashCode() ?: 0)
        result = 31 * result + (episodeType?.hashCode() ?: 0)
        result = 31 * result + (langType?.hashCode() ?: 0)
        result = 31 * result + (episodeId?.hashCode() ?: 0)
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
        return "WakanimEpisode(releaseDate=$releaseDate, anime=$anime, season=$season, number=$number, episodeType=$episodeType, langType=$langType, episodeId=$episodeId, image=$image, duration=$duration, url=$url, platformHandler=$platformHandler, platform=$platform, countryHandler=$countryHandler, country=$country)"
    }
}