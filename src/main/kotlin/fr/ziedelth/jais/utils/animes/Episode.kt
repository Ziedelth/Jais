/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.JLogger
import java.awt.image.BufferedImage
import java.net.URL
import java.util.logging.Level
import javax.imageio.ImageIO

data class Episode(
    val platform: Platform,
    val calendar: String,
    var anime: String,
    val number: String,
    val country: Country,
    val type: EpisodeType = EpisodeType.SUBTITLED,
    var season: String,

    val episodeId: Long,
    val title: String?,
    val image: String?,
    val url: String?,
    val duration: Long = 1440
) {
    @Transient
    var downloadedImage: BufferedImage? = null

    init {
        this.anime = this.anime.replace("’", "'")
        this.season = this.season.replace(" ", "")

        try {
            this.downloadedImage = ImageIO.read(URL(this.image))
        } catch (exception: Exception) {
            JLogger.log(Level.WARNING, "Can not download image", exception)
        }
    }

    override fun toString(): String {
        return "Episode(platform=$platform, calendar='$calendar', anime='$anime', number='$number', country=$country, type=$type, season='$season', episodeId=$episodeId, title=$title, image=$image, url=$url, duration=$duration)"
    }
}