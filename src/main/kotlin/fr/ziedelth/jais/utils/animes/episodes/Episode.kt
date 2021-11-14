/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes.episodes

import fr.ziedelth.jais.Jais
import fr.ziedelth.jais.utils.FileImpl
import fr.ziedelth.jais.utils.ISO8601
import fr.ziedelth.jais.utils.Impl
import fr.ziedelth.jais.utils.animes.countries.CountryImpl
import fr.ziedelth.jais.utils.animes.episodes.datas.AnimeGenre
import fr.ziedelth.jais.utils.animes.episodes.datas.EpisodeType
import fr.ziedelth.jais.utils.animes.episodes.datas.LangType
import fr.ziedelth.jais.utils.animes.platforms.PlatformImpl
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

data class Episode(
    val platform: String,
    val country: String,
    var releaseDate: String,
    var anime: String,
    var animeImage: String?,
    val animeGenres: Array<AnimeGenre>,
    val animeDescription: String?,
    val season: Long,
    var number: Long,
    val episodeType: EpisodeType,
    val langType: LangType,

    var eId: String,
    val title: String?,
    val url: String?,
    val image: String?,
    val duration: Long
) {
    @Transient
    var platformImpl: PlatformImpl? = null

    @Transient
    var countryImpl: CountryImpl? = null

    @Transient
    var animeBufferedImage: BufferedImage? = null

    @Transient
    var episodeBufferedImage: BufferedImage? = null

    init {
        this.releaseDate = ISO8601.toUTCDate(this.releaseDate)
        this.anime = this.anime.replace("’", "'")

        val countryInformation = Jais.getCountryInformation(this.country)
        this.eId = "${
            this.platform.uppercase().substring(0 until 4)
        }-${this.eId}-${this.langType.getData(countryInformation?.country?.javaClass)?.data}"

        this.platformImpl = Jais.getPlatformInformation(this.platform)
        this.countryImpl = Jais.getCountryInformation(this.country)
        Impl.tryCatch { this.animeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.animeImage)), 350, 500) }
        Impl.tryCatch { this.episodeBufferedImage = FileImpl.resizeImage(ImageIO.read(URL(this.image)), 640, 360) }
    }
}