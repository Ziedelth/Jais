package fr.ziedelth.ziedbot.utils.animes

import fr.ziedelth.ziedbot.utils.Const
import net.dv8tion.jda.api.entities.Message
import java.nio.charset.Charset

class Episode(
    var platform: String,
    var calendar: String,
    var title: String?,
    var image: String,
    var link: String,
    var number: String
) {
    @Transient
    lateinit var p: Platform

    @Transient
    lateinit var anime: String

    @Transient
    var message: Message? = null

    fun getId(): String =
        Const.encode("$platform$calendar$title$image$link$number".toByteArray(Charset.defaultCharset()))

    override fun toString(): String {
        return "Episode(platform=$platform, calendar=$calendar, number='$number')"
    }
}