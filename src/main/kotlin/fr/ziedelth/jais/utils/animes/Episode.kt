/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

data class Episode(
    val platform: PlatformImpl,
    val country: CountryImpl,
    val releaseDate: Calendar,
    var anime: String,
    val animeImage: String?,
    val animeGenres: Array<Genre>,
    val animeDescription: String?,
    val season: Long,
    val number: Long,
    val episodeType: EpisodeType,
    val langType: LangType,
    var episodeId: String,
    val title: String?,
    val url: String,
    val image: String,
    val duration: Long,
) {
    var animeBufferedImage: BufferedImage? = null
    var episodeBufferedImage: BufferedImage? = null

    init {
        this.anime = this.anime.replace("’", "'")
        this.episodeId = "${
            this.platform.platformHandler.name.uppercase().substring(0 until 4)
        }-${this.episodeId}-${this.langType.getData(country.country.javaClass)?.data}"

        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
        Impl.tryCatch { this.episodeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.image)), 640, 360) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

        if (platform != other.platform) return false
        if (country != other.country) return false
        if (releaseDate != other.releaseDate) return false
        if (anime != other.anime) return false
        if (animeImage != other.animeImage) return false
        if (!animeGenres.contentEquals(other.animeGenres)) return false
        if (animeDescription != other.animeDescription) return false
        if (season != other.season) return false
        if (number != other.number) return false
        if (episodeType != other.episodeType) return false
        if (langType != other.langType) return false
        if (episodeId != other.episodeId) return false
        if (title != other.title) return false
        if (url != other.url) return false
        if (image != other.image) return false
        if (duration != other.duration) return false
        if (animeBufferedImage != other.animeBufferedImage) return false
        if (episodeBufferedImage != other.episodeBufferedImage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + releaseDate.hashCode()
        result = 31 * result + anime.hashCode()
        result = 31 * result + (animeImage?.hashCode() ?: 0)
        result = 31 * result + animeGenres.contentHashCode()
        result = 31 * result + (animeDescription?.hashCode() ?: 0)
        result = 31 * result + season.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + episodeType.hashCode()
        result = 31 * result + langType.hashCode()
        result = 31 * result + episodeId.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + url.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (animeBufferedImage?.hashCode() ?: 0)
        result = 31 * result + (episodeBufferedImage?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Episode(platform=$platform, country=$country, releaseDate=$releaseDate, anime='$anime', animeImage=$animeImage, animeGenres=${animeGenres.contentToString()}, animeDescription=$animeDescription, season=$season, number=$number, episodeType=$episodeType, langType=$langType, episodeId='$episodeId', title=$title, url='$url', image='$image', duration=$duration, animeBufferedImage=$animeBufferedImage, episodeBufferedImage=$episodeBufferedImage)"
    }
}