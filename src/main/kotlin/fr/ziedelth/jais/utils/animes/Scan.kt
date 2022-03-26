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

/**
 * It's a data class that represents a scan of a manga.
 */
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
    /* It's a nullable variable. */
    var animeBufferedImage: BufferedImage? = null

    /* It's a constructor. */
    init {
        this.anime = this.anime.replace("’", "'")

        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
    }

    /**
     * The function checks if the two objects are the same
     *
     * @param other Any?
     * @return Nothing.
     */
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
        if (animeBufferedImage != other.animeBufferedImage) return false

        return true
    }

    /**
     * The function returns a hash code for the object
     *
     * @return Nothing.
     */
    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + releaseDate.hashCode()
        result = 31 * result + anime.hashCode()
        result = 31 * result + animeImage.hashCode()
        result = 31 * result + animeGenres.contentHashCode()
        result = 31 * result + (animeDescription?.hashCode() ?: 0)
        result = 31 * result + number.hashCode()
        result = 31 * result + episodeType.hashCode()
        result = 31 * result + langType.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (animeBufferedImage?.hashCode() ?: 0)
        return result
    }
}