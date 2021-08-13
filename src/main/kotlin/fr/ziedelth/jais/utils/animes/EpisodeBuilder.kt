package fr.ziedelth.jais.utils.animes

import java.util.*

class EpisodeBuilder(var platform: Platform, var country: Country) {
    var calendar: Calendar? = null
    var anime: String? = null
    var number: String? = null
    var type: EpisodeType? = null
    var season: String? = null
    var episodeId: Long = 0
    var title: String? = null
    var image: String? = null
    var url: String? = null
    var duration: Long = 1440

    fun set(episodeBuilder: EpisodeBuilder) {
        this.platform = episodeBuilder.platform
        this.country = episodeBuilder.country
        this.calendar = episodeBuilder.calendar
        this.anime = episodeBuilder.anime
        this.number = episodeBuilder.number
        this.type = episodeBuilder.type
        this.season = episodeBuilder.season
        this.episodeId = episodeBuilder.episodeId
        this.title = episodeBuilder.title
        this.image = episodeBuilder.image
        this.url = episodeBuilder.url
        this.duration = episodeBuilder.duration
    }

    override fun toString(): String {
        return "EpisodeBuilder(platform=$platform, country=$country, calendar=$calendar, anime=$anime, number=$number, type=$type, season=$season, episodeId=$episodeId, title=$title, image=$image, url=$url, duration=$duration)"
    }
}
