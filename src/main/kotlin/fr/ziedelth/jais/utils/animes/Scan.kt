/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.HashUtils
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

data class Scan(
    val platform: Pair<PlatformHandler, Platform>,
    val country: Pair<CountryHandler, Country>,
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
    private val hash: String = HashUtils.hashString(
        "${
            this.platform.first.name.uppercase().substring(0 until 4)
        }${ISO8601.fromUTCDate(this.releaseDate)}${this.anime}${this.number}${this.langType.getData(country.second.javaClass)?.second}"
    )
    val scanId: String = "${this.platform.first.name.uppercase().substring(0 until 4)}-${
        this.hash.substring(
            0,
            12
        )
    }-${this.langType.getData(country.second.javaClass)?.second}"
    var animeBufferedImage: BufferedImage? = null

    init {
        this.anime = this.anime.replace("’", "'")

        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
    }
}