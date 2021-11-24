/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes.platforms

import com.google.gson.JsonObject
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.animes.AnimeGenre
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.episodes.Episode
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
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
    data class Thumbnail(val url: String?, val width: String?, val height: String?)

    var platformImpl: PlatformImpl? = null
    var countryImpl: CountryImpl? = null

    fun isValid(): Boolean {
        return this.platformImpl != null &&
                this.countryImpl != null &&
                ISO8601.fromUTCDate(ISO8601.toUTCDate(ISO8601.fromCalendar(ISO8601.toCalendar2(this.pubDate)))) != null &&
                !this.seriesTitle.isNullOrBlank() &&
                this.restriction != null && this.restriction.get("").asString.split(" ")
            .contains(this.countryImpl!!.country.restrictionEpisodes(this.platformImpl!!.platform)) &&
                (this.title?.contains(
                    "(${
                        LangType.getLangType(this.countryImpl!!.country::class.java)
                            .getData(this.countryImpl!!.country::class.java)?.data
                    })", true
                ) == true || (!this.subtitleLanguages.isNullOrBlank() && this.subtitleLanguages.split(",")
                    .contains(this.countryImpl!!.country.subtitlesEpisodes(this.platformImpl!!.platform)))) &&
                !this.mediaId.isNullOrBlank() &&
                !this.link.isNullOrBlank() &&
                !this.thumbnail.isNullOrEmpty()
    }

    fun toEpisode(): Episode? {
        return if (this.isValid()) Episode(
            platform = this.platformImpl!!,
            country = this.countryImpl!!,
            releaseDate = ISO8601.fromUTCDate(ISO8601.toUTCDate(ISO8601.fromCalendar(ISO8601.toCalendar2(this.pubDate))))!!,
            anime = this.seriesTitle!!,
            animeImage = this.seriesImage!!.replace("http://", "https://"),
            animeGenres = AnimeGenre.getGenres(this.keywords?.split(", ")?.toTypedArray() ?: emptyArray()),
            animeDescription = this.description,
            season = this.season?.toLongOrNull() ?: 1,
            number = this.episodeNumber?.toLongOrNull() ?: -1,
            episodeType = if ((this.episodeNumber?.toLongOrNull()
                    ?: -1L) != -1L
            ) EpisodeType.EPISODE else EpisodeType.SPECIAL,
            langType = if (this.countryImpl!!.country.subtitlesEpisodes(this.platformImpl!!.platform) == this.subtitleLanguages || this.title?.contains(
                    "(${
                        LangType.getLangType(this.countryImpl!!.country::class.java)
                            .getData(this.countryImpl!!.country::class.java)?.data
                    })",
                    true
                ) == true
            ) LangType.VOICE else LangType.SUBTITLES,
            episodeId = this.mediaId!!,
            title = this.episodeTitle,
            url = this.link!!.replace("http://", "https://"),
            image = this.thumbnail!!.maxByOrNull {
                it.width?.toLongOrNull()?.times(it.height?.toLongOrNull() ?: 0) ?: 0
            }?.url?.replace("http://", "https://")!!,
            duration = this.duration ?: -1,
        ) else null
    }
}
