package fr.ziedelth.jais.utils.animes

data class Episode(
    val platform: Platform,
    val calendar: String,
    val anime: String,
    val number: String,
    val country: Country,
    val type: EpisodeType = EpisodeType.SUBTITLED,
    val season: String,

    val episodeId: Long,
    val title: String?,
    val image: String?,
    val url: String?,
    val duration: Long = 1440
) {
    override fun toString(): String {
        return "Episode(platform=$platform, calendar='$calendar', anime='$anime', number='$number', country=$country, type=$type, season='$season', episodeId=$episodeId, title=$title, image=$image, url=$url, duration=$duration)"
    }
}