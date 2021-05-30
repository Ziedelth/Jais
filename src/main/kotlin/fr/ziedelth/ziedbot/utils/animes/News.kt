package fr.ziedelth.ziedbot.utils.animes

class News(
    var platform: String,
    var calendar: String,
    val title: String,
    val description: String,
    val content: String,
    val link: String
) {
    @Transient
    lateinit var p: Platform

    override fun toString(): String {
        return "News(platform=$platform, calendar=$calendar, title='$title', description='$description', content='$content', link='$link')"
    }
}