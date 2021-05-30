package fr.ziedelth.ziedbot.utils.animes

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

    override fun toString(): String {
        return "Episode(platform='$platform', calendar='$calendar', title=$title, image='$image', link='$link', number='$number')"
    }
}