/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import com.google.gson.JsonObject
import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl

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
    val keywords: String?,

    var description: String? = null,
) {
    data class Thumbnail(
        val url: String?,
        val width: String?,
        val height: String?
    )

    var platformImpl: PlatformImpl? = null
    var platform: Platform? = null
        set(value) {
            field = value
            this.platformImpl = Jais.getPlatformInformation(value)
        }
    var countryImpl: CountryImpl? = null
    var country: Country? = null
        set(value) {
            field = value
            this.countryImpl = Jais.getCountryInformation(value)
        }

    fun isValid(): Boolean {
        return this.platform != null &&
                this.country != null &&
                !this.pubDate.isNullOrBlank() &&
                !this.seriesTitle.isNullOrBlank() &&
                !this.episodeNumber.isNullOrBlank() &&
                this.restriction != null && this.restriction.get("").asString.split(" ")
            .contains(this.country!!.restrictionEpisodes(this.platform!!)) && (this.title?.contains(
            "(${
                LangType.getLangType(
                    this.countryImpl?.country?.javaClass
                ).getData(this.countryImpl?.country?.javaClass)?.data
            })", true
        ) == true || (!this.subtitleLanguages.isNullOrBlank() && this.subtitleLanguages.split(",")
            .contains(this.country!!.subtitlesEpisodes(this.platform!!)))) &&
                !this.mediaId.isNullOrBlank() &&
                this.duration != null
    }

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformImpl!!.platformHandler.name,
            country = this.countryImpl!!.countryHandler.name,
            releaseDate = ISO8601.fromCalendar2(this.pubDate)!!,
            anime = this.seriesTitle!!,
            animeImage = this.seriesImage?.replace("http://", "https://")?.replace("http://", "https://"),
            animeGenres = AnimeGenre.getGenres(this.keywords?.split(", ")?.toTypedArray() ?: emptyArray()),
            animeDescription = this.description,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.episodeNumber?.toLongOrNull() ?: -1,
            episodeType = if ((this.episodeNumber?.toLongOrNull()
                    ?: -1L) != -1L
            ) EpisodeType.EPISODE else EpisodeType.SPECIAL,
            langType = if (this.country?.subtitlesEpisodes(this.platform!!) == this.subtitleLanguages || this.title?.contains(
                    "(${
                        LangType.getLangType(this.countryImpl?.country?.javaClass)
                            .getData(this.countryImpl?.country?.javaClass)?.data
                    })",
                    true
                ) == true
            ) LangType.VOICE else LangType.SUBTITLES,

            eId = this.mediaId!!,
            title = this.episodeTitle,
            url = this.link?.replace("http://", "https://"),
            image = this.thumbnail?.maxByOrNull {
                it.width?.toLongOrNull()?.times(it.height?.toLongOrNull() ?: 0) ?: 0
            }?.url?.replace("http://", "https://"),
            duration = this.duration!!,
        ) else null
    }
}
