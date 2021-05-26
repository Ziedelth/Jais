package fr.ziedelth.ziedbot.utils.animes

import fr.ziedelth.ziedbot.utils.Const
import java.nio.charset.Charset
import java.util.*

class News(
    val platform: Platform,
    val calendar: Calendar,
    val title: String,
    val description: String,
    val content: String,
    val link: String
) {
    val id: String = Const.encode(
        (this.platform.getName() + this.calendar.toString() + this.title + this.description + this.link + this.content).toByteArray(
            Charset.defaultCharset()
        )
    )
}