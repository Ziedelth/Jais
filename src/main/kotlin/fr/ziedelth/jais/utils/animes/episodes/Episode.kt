/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
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
    val animeImage: String,
    val animeGenres: Array<AnimeGenre>,
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
}