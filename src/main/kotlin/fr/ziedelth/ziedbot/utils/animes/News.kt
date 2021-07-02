package fr.ziedelth.ziedbot.utils.animes

class News(
    val platform: String,
    val calendar: String,
    val title: String,
    val description: String,
    val content: String,
    val link: String,
    val country: Country
) {
    @Transient
    var p: Platform? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as News

        if (platform != other.platform) return false
        if (calendar != other.calendar) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (content != other.content) return false
        if (link != other.link) return false
        if (country != other.country) return false

        return true
    }

    override fun toString(): String {
        return "News(platform='$platform', calendar='$calendar', title='$title', description='$description', content='$content', link='$link', language=$country)"
    }

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + country.hashCode()
        return result
    }
}