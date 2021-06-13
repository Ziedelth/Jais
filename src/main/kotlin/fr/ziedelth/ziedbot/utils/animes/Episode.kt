package fr.ziedelth.ziedbot.utils.animes

import net.dv8tion.jda.api.entities.Message

class Episode(
    val id: String,
    val platform: String,
    val calendar: String,
    var anime: String,
    var title: String?,
    var image: String,
    var link: String,
    var number: String
) {
    @Transient
    val messages: MutableList<Message> = mutableListOf()

    @Transient
    var p: Platform? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

        if (id != other.id) return false
        if (platform != other.platform) return false
        if (calendar != other.calendar) return false
        if (anime != other.anime) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (link != other.link) return false
        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + platform.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + anime.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + image.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + number.hashCode()
        return result
    }

    override fun toString(): String {
        return "Episode(id='$id', platform='$platform', calendar='$calendar', anime='$anime', title=$title, image='$image', link='$link', number='$number')"
    }
}