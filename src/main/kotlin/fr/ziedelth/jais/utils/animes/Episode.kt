package fr.ziedelth.jais.utils.animes

import fr.ziedelth.jais.utils.Const

class Episode(
    val platform: String,
    val calendar: String,
    val anime: String,
    val number: String,
    val country: Country,
    val type: EpisodeType = EpisodeType.SUBTITLED,
    var id: String?,
    var title: String?,
    var image: String?,
    var link: String?,
    var duration: Long = 1440
) {
    val globalId: String =
        Const.substring(
            Const.encodeSHA512("$platform$calendar$anime$number${country.name}${type.name}".toByteArray()),
            32
        )

    @Transient
    var p: Platform? = null

    fun edit(other: Episode) {
        this.id = other.id
        this.title = other.title
        this.image = other.image
        this.link = other.link
        this.duration = other.duration
        this.p = other.p
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

        if (platform != other.platform) return false
        if (calendar != other.calendar) return false
        if (anime != other.anime) return false
        if (number != other.number) return false
        if (country != other.country) return false
        if (type != other.type) return false
        if (id != other.id) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (link != other.link) return false
        if (duration != other.duration) return false
        if (globalId != other.globalId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + anime.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + globalId.hashCode()
        result = 31 * result + (p?.hashCode() ?: 0)
        return result
    }
}