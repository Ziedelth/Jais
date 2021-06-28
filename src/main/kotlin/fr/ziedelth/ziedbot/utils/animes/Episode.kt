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

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + anime.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + image.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + globalId.hashCode()
        result = 31 * result + messages.hashCode()
        result = 31 * result + (p?.hashCode() ?: 0)
        return result
    }
}