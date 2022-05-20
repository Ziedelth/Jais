package fr.ziedelth.jais.utils

data class Episode(
    val platform: IPlatform,
    val anime: Anime,
    val releaseDate: String,
    val season: Int,
    val number: Int,
    var episodeId: String,
    val title: String?,
    val url: String,
    val image: String,
    val duration: Long
) {
    init {
        episodeId = "${platform.name.uppercase().substring(0 until 4)}-${episodeId}"
    }

    override fun toString(): String {
        return "Episode(anime=$anime, releaseDate='$releaseDate', season=$season, number=$number, episodeId='$episodeId', title=$title, url='$url', image='$image', duration=$duration)"
    }
}
