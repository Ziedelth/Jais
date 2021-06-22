package fr.ziedelth.ziedbot.utils.animes

enum class Language(
    val flag: String,
    val episode: String,
    val subtitles: String,
    val voice: String,
    val country: String,
    val lang: String,
    val language: String
) {
    FRENCH("🇫🇷", "Épisode", "VOSTFR", "VF", "fr", "frFR", "fr - fr"),
    ;
}