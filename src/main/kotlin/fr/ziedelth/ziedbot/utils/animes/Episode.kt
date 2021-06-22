package fr.ziedelth.ziedbot.utils.animes

import fr.ziedelth.ziedbot.utils.Const

class Episode(
    val platform: String,
    val calendar: String,
    val anime: String,
    var id: String,
    var title: String?,
    var image: String,
    var link: String,
    val number: String,
    val language: Language,
    val type: EpisodeType = EpisodeType.SUBTITLES
) {
    val globalId: String = Const.encode("$platform$calendar$anime$number${language.name}${type.name}".toByteArray())
    val messages: MutableList<Long> = mutableListOf()

    @Transient
    var p: Platform? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

        if (globalId != other.globalId) return false
        if (platform != other.platform) return false
        if (calendar != other.calendar) return false
        if (anime != other.anime) return false
        if (id != other.id) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (link != other.link) return false
        if (number != other.number) return false
        if (language != other.language) return false
        if (type != other.type) return false

        return true
    }
}