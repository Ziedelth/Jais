/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO


data class Scan(
    val platform: PlatformImpl,
    val country: CountryImpl,
    val releaseDate: Calendar,
    var anime: String,
    val animeImage: String?,
    val animeGenres: Array<Genre>,
    val animeDescription: String?,
    val number: Long,
    val episodeType: EpisodeType = EpisodeType.CHAPTER,
    val langType: LangType = LangType.SUBTITLES,
    val url: String,
) {
    private val hash: String = HashUtils.sha512("${this.platform.platformHandler.name.uppercase().substring(0 until 4)}${ISO8601.fromUTCDate(this.releaseDate)}${this.anime}${this.number}${this.langType.getData(country.country.javaClass)?.data}")
    val scanId: String = "${this.platform.platformHandler.name.uppercase().substring(0 until 4)}-${
        this.hash.substring(
            0,
            12
        )
    }-${this.langType.getData(country.country.javaClass)?.data}"
    var animeBufferedImage: BufferedImage? = null

    init {
        this.anime = this.anime.replace("’", "'")

        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Scan

        if (platform != other.platform) return false
        if (country != other.country) return false
        if (releaseDate != other.releaseDate) return false
        if (anime != other.anime) return false
        if (animeImage != other.animeImage) return false
        if (!animeGenres.contentEquals(other.animeGenres)) return false
        if (animeDescription != other.animeDescription) return false
        if (number != other.number) return false
        if (episodeType != other.episodeType) return false
        if (langType != other.langType) return false
        if (url != other.url) return false
        if (hash != other.hash) return false
        if (scanId != other.scanId) return false
        if (animeBufferedImage != other.animeBufferedImage) return false

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
        result = 31 * result + number.hashCode()
        result = 31 * result + episodeType.hashCode()
        result = 31 * result + langType.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + scanId.hashCode()
        result = 31 * result + (animeBufferedImage?.hashCode() ?: 0)
        return result
    }
}