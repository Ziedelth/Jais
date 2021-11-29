/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.scans

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.EpisodeType
import fr.ziedelth.jais.utils.animes.Genre
import fr.ziedelth.jais.utils.animes.LangType
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
    val animeImage: String,
    val genres: Array<Genre>,
    val animeDescription: String?,
    val number: Long,
    val episodeType: EpisodeType = EpisodeType.CHAPTER,
    val langType: LangType = LangType.SUBTITLES,
    val url: String,
) {
    var animeBufferedImage: BufferedImage? = null

    init {
        this.anime = this.anime.replace("’", "'")

        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
    }
}