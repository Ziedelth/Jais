/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.Country
import fr.ziedelth.jais.utils.animes.countries.CountryHandler
import fr.ziedelth.jais.utils.animes.platforms.Platform
import fr.ziedelth.jais.utils.animes.platforms.PlatformHandler
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

data class Episode(
    val platform: Pair<PlatformHandler, Platform>,
    val country: Pair<CountryHandler, Country>,
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
            this.platform.first.name.uppercase().substring(0 until 4)
        }-${this.episodeId}-${this.langType.getData(country.second.javaClass)?.second}"

        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
        Impl.tryCatch { this.episodeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.image)), 640, 360) }
    }
}